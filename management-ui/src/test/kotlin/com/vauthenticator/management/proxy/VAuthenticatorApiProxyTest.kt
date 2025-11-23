package com.vauthenticator.management.proxy

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.servlet.HandlerMapping

@WireMockTest
class VAuthenticatorApiProxyTest {

    @Test
    fun `do a service call without body`(wm : WireMockRuntimeInfo)  {
        stubFor(
            get("/api/something")
                .willReturn(
                    ok("echo")
                )
        )

        val apiProxy = VAuthenticatorApiProxy(
            "http://localhost:${wm.httpPort}/api",
            ApiServiceCallProxyService("/secure/api"),
            RestTemplate()
        )

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.setAttribute(
            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
            "/secure/api/something"
        )
        mockHttpServletRequest.addHeader("Content-Type", "text/html")
        val servletWebRequest = ServletWebRequest(mockHttpServletRequest, MockHttpServletResponse())
        val proxy = apiProxy.proxy(servletWebRequest, HttpMethod.GET, null)
        val body: ByteArray = proxy.body!!
        assertEquals("echo", String(body))
    }

    @Test
    fun `do a service call with body`(wm : WireMockRuntimeInfo)  {
        stubFor(
            post("/api/something")
                .withRequestBody(equalTo("hello"))
                .willReturn(
                    ok("echo")
                )
        )

        val apiProxy = VAuthenticatorApiProxy(
            "http://localhost:${wm.httpPort}/api",
            ApiServiceCallProxyService("/secure/api"),
            RestTemplate()
        )

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.setAttribute(
            HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE,
            "/secure/api/something"
        )
        mockHttpServletRequest.addHeader("Content-Type", "text/html")
        val servletWebRequest = ServletWebRequest(mockHttpServletRequest, MockHttpServletResponse())
        val proxy = apiProxy.proxy(servletWebRequest, HttpMethod.POST, "hello")
        val body: ByteArray = proxy.body!!
        assertEquals("echo", String(body))
    }
}