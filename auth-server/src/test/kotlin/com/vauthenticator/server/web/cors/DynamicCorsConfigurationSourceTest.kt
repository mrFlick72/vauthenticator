package com.vauthenticator.server.web.cors

import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class DynamicCorsConfigurationSourceTest {

    @MockK
    lateinit var corsConfigurationResolver: CorsConfigurationResolver

    val clientAppId = ClientAppFixture.aClientAppId()

    lateinit var uut: DynamicCorsConfigurationSource

    @BeforeEach
    fun setUp() {
        uut = DynamicCorsConfigurationSource(corsConfigurationResolver)
    }

    @Test
    fun `when the origin is allowed`() {
        val origin = "http://example.com"
        val request = requestFrom(origin)
        every { corsConfigurationResolver.configurationFor(any()) } returns AuthServerCorsConfiguration(origin)

        val actual = uut.getCorsConfiguration(request)

        assertEquals(listOf(AuthServerCorsConfiguration(origin).allowedOrigin), actual.allowedOrigins)
        assertEquals(AuthServerCorsConfiguration(origin).allowCredentials, actual.allowCredentials)
        assertEquals(AuthServerCorsConfiguration(origin).maxAge, actual.maxAge)
        assertEquals(AuthServerCorsConfiguration(origin).allowCredentials, actual.allowCredentials)
    }

    private fun requestFrom(origin: String): MockHttpServletRequest {
        val request = MockHttpServletRequest()
        request.method = "GET"
        request.addHeader("Origin", origin)
        return request
    }


}