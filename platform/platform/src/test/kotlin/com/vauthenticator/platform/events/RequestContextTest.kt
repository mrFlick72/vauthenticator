package com.vauthenticator.platform.events

import com.vauthenticator.platform.extensions.toSha256
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class RequestContextTest {

    @BeforeEach
    @AfterEach
    fun resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `the context of the current request references the session by its SHA-256, never by its id`() {
        val request = MockHttpServletRequest().apply {
            remoteAddr = "10.0.0.1"
            addHeader("User-Agent", "a browser")
            setSession(MockHttpSession(null, "A_SESSION_ID"))
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        assertEquals(RequestContext("10.0.0.1", "a browser", "A_SESSION_ID".toSha256()), RequestContext.current())
    }

    @Test
    fun `without a request there is no context`() {
        assertNull(RequestContext.current())
    }
}
