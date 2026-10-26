package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.mfa.domain.MfaException
import com.vauthenticator.server.mfa.domain.MfaFailureEvent
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken

private const val USER_NAME = "email@domain.com"

@ExtendWith(MockKExtension::class)
class FailedLoginLockAuthenticationListenerTest {

    @MockK
    lateinit var failedLoginLock: FailedLoginLock

    @MockK
    lateinit var currentHttpServletRequestService: CurrentHttpServletRequestService

    lateinit var uut: FailedLoginLockAuthenticationListener

    private val passwordLogin = UsernamePasswordAuthenticationToken.unauthenticated(USER_NAME, "a wrong password")

    @BeforeEach
    fun setUp() {
        uut = FailedLoginLockAuthenticationListener(failedLoginLock, currentHttpServletRequestService)
    }

    @Test
    fun `when a password login fails with bad credentials it is a failed login`() {
        val request = MockHttpServletRequest().apply { addParameter("client_id", "A_CLIENT_APP_ID") }
        every { currentHttpServletRequestService.getServletRequest() } returns request
        every { failedLoginLock.failedLogin(USER_NAME, ClientAppId("A_CLIENT_APP_ID")) } just runs

        uut.onFailure(AuthenticationFailureBadCredentialsEvent(passwordLogin, BadCredentialsException("bad")))

        verify { failedLoginLock.failedLogin(USER_NAME, ClientAppId("A_CLIENT_APP_ID")) }
    }

    @Test
    fun `when a wrong mfa code is given it is not a failed login`() {
        uut.onFailure(MfaFailureEvent(passwordLogin, MfaException("bad code")))

        verify(exactly = 0) { failedLoginLock.failedLogin(any(), any()) }
    }

    @Test
    fun `when a password login succeeds the password is proven`() {
        every { failedLoginLock.passwordProven(USER_NAME) } just runs

        uut.onSuccess(AuthenticationSuccessEvent(passwordLogin))

        verify { failedLoginLock.passwordProven(USER_NAME) }
    }

    @Test
    fun `when a bearer token authentication succeeds the password is not proven`() {
        uut.onSuccess(AuthenticationSuccessEvent(BearerTokenAuthenticationToken("a token")))

        verify(exactly = 0) { failedLoginLock.passwordProven(any()) }
    }
}
