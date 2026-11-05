package com.vauthenticator.server.config

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.web.util.matcher.RequestMatcher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthorizationServerRequestMatcherTest {

    private val endpointsMatcher = RequestMatcher { it.method == "GET" && it.requestURI == "/userinfo" }
    private val uut = authorizationServerRequestMatcher(endpointsMatcher, "/userinfo")

    private fun request(method: String, uri: String) = MockHttpServletRequest(method, uri)

    @Test
    fun `the authorization server endpoints are matched`() {
        assertTrue(uut.matches(request("GET", "/userinfo")))
    }

    @Test
    fun `the userinfo CORS preflight is matched, so the chain applies CORS to it`() {
        assertTrue(uut.matches(request("OPTIONS", "/userinfo")))
    }

    @Test
    fun `a preflight to another path is not matched`() {
        assertFalse(uut.matches(request("OPTIONS", "/api/roles")))
    }

    @Test
    fun `another method on userinfo is left to the endpoints matcher`() {
        assertFalse(uut.matches(request("DELETE", "/userinfo")))
    }
}
