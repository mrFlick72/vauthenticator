package com.vauthenticator.server.web.cors

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class CorsConfigurationResolverTest {
    lateinit var uut: CorsConfigurationResolver

    @MockK
    lateinit var allowedOriginRepository: AllowedOriginRepository

    @BeforeEach
    fun setup() {
        uut = CorsConfigurationResolver(allowedOriginRepository)
    }


    @ParameterizedTest
    @ValueSource(strings = ["https://example.com","http://example.com", "http://local.example.com:9090"])
    fun `when there is an origin header defined`(origin : String) {
        every { allowedOriginRepository.getAllAvailableAllowedOrigins() } returns setOf(AllowedOrigin(origin))

        val actual = uut.configurationFor(requestFrom(origin))

        assertEquals(AuthServerCorsConfiguration(origin), actual)
    }


    @ParameterizedTest
    @ValueSource(strings = ["https://example.com","http://example.com", "http://local.example.com:9090"])
    fun `when there is not an origin header defined`(origin : String) {
        every { allowedOriginRepository.getAllAvailableAllowedOrigins() } returns setOf(AllowedOrigin(origin))

        val actual = uut.configurationFor(MockHttpServletRequest())

        assertEquals(AuthServerCorsConfiguration(""), actual)
    }


    @ParameterizedTest
    @ValueSource(strings = ["https://example.com","http://example.com", "http://local.example.com:9090"])
    fun `when there is an origin header defined but it is not allowed`(origin : String) {
        val currentOrigin = "http://not.allowed.origin.com"
        every { allowedOriginRepository.getAllAvailableAllowedOrigins() } returns setOf(AllowedOrigin(origin))

        val actual = uut.configurationFor(requestFrom(currentOrigin))

        assertEquals(AuthServerCorsConfiguration(""), actual)
    }


    @Test
    fun `when there is an origin header defined but there are not allowed origins`() {
        val currentOrigin = "http://not.allowed.origin.com"
        every { allowedOriginRepository.getAllAvailableAllowedOrigins() } returns emptySet()

        val actual = uut.configurationFor(requestFrom(currentOrigin))

        assertEquals(AuthServerCorsConfiguration(""), actual)
    }

    private fun requestFrom(origin: String): MockHttpServletRequest {
        val request = MockHttpServletRequest()
        request.addHeader("Origin", origin)
        return request
    }


}