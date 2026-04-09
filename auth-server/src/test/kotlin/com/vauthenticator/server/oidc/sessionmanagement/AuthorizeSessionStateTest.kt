package com.vauthenticator.server.oidc.sessionmanagement

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken
import org.springframework.security.web.RedirectStrategy

@ExtendWith(MockKExtension::class)
class AuthorizeSessionStateTest {

    @MockK(relaxed = true)
    private lateinit var redisTemplate: RedisTemplate<String, String?>

    @MockK(relaxed = true)
    private lateinit var factory: SessionManagementFactory

    @MockK(relaxed = true)
    private lateinit var redirectStrategy: RedirectStrategy

    @MockK(relaxed = true)
    private lateinit var request: HttpServletRequest

    @MockK(relaxed = true)
    private lateinit var response: HttpServletResponse

    @MockK(relaxed = true)
    private lateinit var authentication: OAuth2AuthorizationCodeRequestAuthenticationToken

    @Test
    fun `when redirect uri is missing then a domain exception is thrown`() {
        every { authentication.redirectUri } returns null

        val actual = assertThrows(SendAuthorizationResponseException::class.java) {
            sendAuthorizationResponse(redisTemplate, factory, redirectStrategy)(request, response, authentication)
        }

        assertEquals("Missing redirect uri in authorization response", actual.message)
        verify(exactly = 0) { redirectStrategy.sendRedirect(any(), any(), any()) }
        verify(exactly = 0) { factory.sessionStateFor(any(), any()) }
        verify(exactly = 0) { factory.sessionIdFor(any()) }
    }

    @Test
    fun `when authorization code is missing then a domain exception is thrown`() {
        every { authentication.redirectUri } returns "https://client.example/callback"
        every { authentication.authorizationCode } returns null

        val actual = assertThrows(SendAuthorizationResponseException::class.java) {
            sendAuthorizationResponse(redisTemplate, factory, redirectStrategy)(request, response, authentication)
        }

        assertEquals("Missing authorization code in authorization response", actual.message)
        verify(exactly = 0) { redirectStrategy.sendRedirect(any(), any(), any()) }
        verify(exactly = 0) { factory.sessionStateFor(any(), any()) }
        verify(exactly = 0) { factory.sessionIdFor(any()) }
    }
}
