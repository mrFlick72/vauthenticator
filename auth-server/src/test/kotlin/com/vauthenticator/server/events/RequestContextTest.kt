package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.support.ClientAppFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.security.MessageDigest
import java.time.Instant

class RequestContextTest {

    // other tests (e.g. AwsLambdaFunctionTest) leave request attributes bound to the thread
    @BeforeEach
    @AfterEach
    fun resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `an event created during a request carries where the request came from`() {
        val session = MockHttpSession(null, "A_SESSION_ID")
        val request = MockHttpServletRequest().apply {
            remoteAddr = "203.0.113.7"
            addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64)")
            setSession(session)
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        val event = anEvent()

        assertEquals(
            RequestContext(
                ipAddress = "203.0.113.7",
                userAgent = "Mozilla/5.0 (X11; Linux x86_64)",
                sessionReference = sha256Hex("A_SESSION_ID")
            ),
            event.requestContext
        )
    }

    @Test
    fun `an event created during a request without a session has no session reference and creates no session`() {
        val request = MockHttpServletRequest().apply { remoteAddr = "203.0.113.7" }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        val event = anEvent()

        assertEquals(RequestContext("203.0.113.7", null, null), event.requestContext)
        assertNull(request.getSession(false))
    }

    @Test
    fun `an event created outside a request has no request context`() {
        val event = anEvent()

        assertNull(event.requestContext)
    }

    private fun anEvent() = SignUpEvent(
        Email("anemail@domain.com"),
        ClientAppFixture.aClientAppId(),
        Instant.now(),
        Password("A_PASSWORD")
    )

    private fun sha256Hex(value: String) =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
