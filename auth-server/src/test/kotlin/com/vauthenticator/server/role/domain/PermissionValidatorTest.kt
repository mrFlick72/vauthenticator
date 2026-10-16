package com.vauthenticator.server.role.domain

import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.InsufficientClientApplicationScopeException
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.web.savedrequest.DefaultSavedRequest

@ExtendWith(MockKExtension::class)
class PermissionValidatorTest {

    private val clientAppId = aClientAppId()
    private val scopes = Scopes.from(Scope("SCOPE"))
    private val scopesValues = scopes.content.map { it.content }


    @MockK
    private lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    private lateinit var session: HttpSession

    private lateinit var uut: PermissionValidator

    @BeforeEach
    fun setUp() {
        uut = PermissionValidator(clientApplicationRepository)
    }

    @Test
    fun `when scopes are validate on client app scopes`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns httpSessionWithClientApp()
        every { clientApplicationRepository.findOne(clientAppId) } returns
                aClientApp(clientAppId).copy(
                    scopes = scopes
                )

        uut.validate(null, session, scopes)
    }

    @Test
    fun `when scopes are not validate on client app scopes`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns httpSessionWithClientApp()
        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp(clientAppId)


        assertThrows(InsufficientClientApplicationScopeException::class.java) { uut.validate(null, session, scopes) }
    }

    @Test
    fun `when scopes are validate on principal scopes`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns emptyHttpSession()

        val principal = SecurityFixture.principalFor(
            clientAppId = clientAppId.content,
            email = EMAIL,
            scopes = scopesValues
        )
        uut.validate(authentication = principal, session = session, scopes = scopes)
    }

    @Test
    fun `when scopes are not validate on principal scopes`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns emptyHttpSession()

        val principal = SecurityFixture.principalFor(
            clientAppId = clientAppId.content,
            email = EMAIL,
            scopes = emptyList()
        )
        assertThrows(InsufficientClientApplicationScopeException::class.java) {
            uut.validate(
                authentication = principal,
                session = session,
                scopes = scopes
            )
        }
    }

    @Test
    fun `when there is no authentication a jwt authentication is required`() {
        assertThrows(JwtAuthenticationRequiredException::class.java) {
            uut.validate(authentication = null, scopes = scopes)
        }
    }

    @Test
    fun `when the authentication is a session user a jwt authentication is required`() {
        val sessionUser = SecurityFixture.principalFor(EMAIL, listOf(Role.adminRole().name))

        assertThrows(JwtAuthenticationRequiredException::class.java) {
            uut.validate(authentication = sessionUser, scopes = scopes)
        }
    }

    @Test
    fun `when scopes are validate on jwt authentication scopes`() {
        val principal = SecurityFixture.principalFor(
            clientAppId = clientAppId.content,
            email = EMAIL,
            scopes = scopesValues
        )

        uut.validate(authentication = principal, scopes = scopes)
    }

    @Test
    fun `when scopes are not validate on jwt authentication scopes`() {
        val principal = SecurityFixture.principalFor(
            clientAppId = clientAppId.content,
            email = EMAIL,
            scopes = emptyList()
        )

        assertThrows(InsufficientClientApplicationScopeException::class.java) {
            uut.validate(authentication = principal, scopes = scopes)
        }
    }

    @Test
    fun `when the jwt authentication is returned once scopes are validated`() {
        val principal = SecurityFixture.principalFor(
            clientAppId = clientAppId.content,
            email = EMAIL,
            scopes = scopesValues
        )

        assertSame(principal, uut.validate(authentication = principal, scopes = scopes))
    }

    @Test
    fun `when a jwt authentication is required and there is no authentication`() {
        assertThrows(JwtAuthenticationRequiredException::class.java) {
            uut.jwtAuthenticationOf(null)
        }
    }

    @Test
    fun `when a jwt authentication is required and the authentication is a session user`() {
        val sessionUser = SecurityFixture.principalFor(EMAIL, listOf(Role.adminRole().name))

        assertThrows(JwtAuthenticationRequiredException::class.java) {
            uut.jwtAuthenticationOf(sessionUser)
        }
    }

    @Test
    fun `when a jwt authentication is required and the authentication is a jwt`() {
        val principal = SecurityFixture.principalFor(clientAppId = clientAppId.content, email = EMAIL)

        assertSame(principal, uut.jwtAuthenticationOf(principal))
    }

    @Test
    fun `when scopes are validate on client app scopes for a session user`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns httpSessionWithClientApp()
        every { clientApplicationRepository.findOne(clientAppId) } returns
                aClientApp(clientAppId).copy(
                    scopes = scopes
                )
        val sessionUser = SecurityFixture.principalFor(EMAIL)

        uut.validate(authentication = sessionUser, session = session, scopes = scopes)
    }

    @Test
    fun `when scopes are not validate on client app scopes for a session user`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns httpSessionWithClientApp()
        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp(clientAppId)
        val sessionUser = SecurityFixture.principalFor(EMAIL, listOf(Role.adminRole().name))

        assertThrows(InsufficientClientApplicationScopeException::class.java) {
            uut.validate(authentication = sessionUser, session = session, scopes = scopes)
        }
    }

    private fun httpSessionWithClientApp(): DefaultSavedRequest =
        DefaultSavedRequest.Builder()
            .setRequestURI("http://localhost")
            .setServerName("localhost")
            .setScheme("http")
            .setParameters(
                mutableMapOf("client_id" to arrayOf(clientAppId.content))
            ).build()

    private fun emptyHttpSession(): DefaultSavedRequest =
        DefaultSavedRequest.Builder()
            .setRequestURI("http://localhost")
            .setServerName("localhost")
            .setScheme("http")
            .build()
}