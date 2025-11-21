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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import java.util.*

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
        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(
            aClientApp(clientAppId).copy(
                scopes = scopes
            )
        )

        uut.validate(null, session, scopes)
    }

    @Test
    fun `when scopes are not validate on client app scopes`() {
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns httpSessionWithClientApp()
        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(
            aClientApp(clientAppId)
        )

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
        uut.validate(principal = principal, session = session, scopes = scopes)
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
                principal = principal,
                session = session,
                scopes = scopes
            )
        }
    }

    private fun httpSessionWithClientApp(): DefaultSavedRequest =
        DefaultSavedRequest.Builder()
            .setParameters(
                mutableMapOf("client_id" to arrayOf(clientAppId.content))
            ).build()

    private fun emptyHttpSession(): DefaultSavedRequest =
        DefaultSavedRequest.Builder().build()
}