package com.vauthenticator.server.web.cors

import com.vauthenticator.server.config.FRONT_CHANNEL_LOG_OUT_URL
import com.vauthenticator.server.config.LOG_IN_URL_PAGE
import com.vauthenticator.server.config.LOG_OUR_URL
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.internalSubstitute
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import kotlin.test.assertFails

@ExtendWith(MockKExtension::class)
class AuthServerCorsFilterTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var corsConfigurationResolver: CorsConfigurationResolver

    @MockK
    lateinit var filterChain: FilterChain

    val clientAppId = ClientAppFixture.aClientAppId()

    lateinit var uut: AuthServerCorsFilter

    @BeforeEach
    fun setUp() {
        val authorizationServerSettings = AuthorizationServerSettings.builder().build()

        val allowedEndpoints = listOf(
            authorizationServerSettings.jwkSetEndpoint,
            authorizationServerSettings.authorizationEndpoint,
            authorizationServerSettings.deviceAuthorizationEndpoint,
            authorizationServerSettings.tokenEndpoint,
            authorizationServerSettings.oidcUserInfoEndpoint,
            authorizationServerSettings.oidcLogoutEndpoint,
            authorizationServerSettings.tokenRevocationEndpoint,
            authorizationServerSettings.tokenIntrospectionEndpoint,
            LOG_IN_URL_PAGE,
            FRONT_CHANNEL_LOG_OUT_URL,
            LOG_OUR_URL
        )

        uut = AuthServerCorsFilter(corsConfigurationResolver, allowedEndpoints)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/oauth2/authorize",
            "/oauth2/device_authorization",
            "/oauth2/token",
            "/oauth2/jwks",
            "/userinfo",
            "/connect/logout",
            "/oauth2/revoke",
            "/oauth2/introspect"
        ]
    )
    fun `when the origin is allowed`(requestUri: String) {
        val origin = "http://example.com"
        val request = requestFrom(origin, requestUri)
        val response = MockHttpServletResponse()
        AuthorizationServerSettings.builder().build()
        every { corsConfigurationResolver.configurationFor(any()) } returns AuthServerCorsConfiguration(origin)
        every { filterChain.doFilter(request, response) } just runs

        uut.doFilter(request, response, filterChain)

        assertTrue { response.getHeaders("Access-Control-Allow-Origin").contains(origin) }
        assertTrue { response.getHeaders("Access-Control-Allow-Methods").contains("GET POST OPTIONS") }
        assertTrue { response.getHeaders("Access-Control-Max-Age").contains("3600") }
        assertTrue { response.getHeaders("Access-Control-Allow-Credentials").contains("true") }

        verify { filterChain.doFilter(request, response) }
    }

    @Test
    fun `when the origin not processed`() {
        val origin = "http://example.com"
        val request = requestFrom(origin, "/excluded/endpoint/")
        val response = MockHttpServletResponse()
        AuthorizationServerSettings.builder().build()
        every { corsConfigurationResolver.configurationFor(any()) } returns AuthServerCorsConfiguration(origin)
        every { filterChain.doFilter(request, response) } just runs

        uut.doFilter(request, response, filterChain)

        assertFalse { response.getHeaders("Access-Control-Allow-Origin").contains(origin) }
        assertFalse { response.getHeaders("Access-Control-Allow-Methods").contains("GET POST OPTIONS") }
        assertFalse { response.getHeaders("Access-Control-Max-Age").contains("3600") }
        assertFalse { response.getHeaders("Access-Control-Allow-Credentials").contains("true") }

        verify { filterChain.doFilter(request, response) }
    }

    private fun requestFrom(origin: String, requestUri: String): MockHttpServletRequest {
        val request = MockHttpServletRequest()
        request.method = "GET"
        request.requestURI = requestUri
        request.addHeader("Origin", origin)
        return request
    }

}