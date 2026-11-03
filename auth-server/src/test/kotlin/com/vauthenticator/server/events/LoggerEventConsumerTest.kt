package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.EventFixture.vauthenticatorAuthEvent
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant

@ExtendWith(OutputCaptureExtension::class)
class LoggerEventConsumerTest {

    @Test
    fun `when a non Default Spring event is fired line MFA event`(output: CapturedOutput) {
        val underTest = LoggerEventConsumer(EventConsumerConfig(mapOf("logger-event-consumer" to true)))
        underTest.accept(vauthenticatorAuthEvent)
        val message = """
            The user anemail@domain.com
            with the client id A_CLIENT_APP_ID
            has done an AuthenticationSuccessEvent event
            event at ${vauthenticatorAuthEvent.timeStamp.epochSecond}
            event payload: AuthenticationSuccessEvent
            """.trimIndent()
        assertTrue(output.out.contains(message))
    }

    @Test
    fun `the request context of the event is logged`(output: CapturedOutput) {
        val event = anEventCreatedInARequest(userAgent = "Mozilla/5.0 (X11; Linux x86_64)")

        underTest.accept(event)

        val message = """
            from the ip address 203.0.113.7
            with the user agent Mozilla/5.0 (X11; Linux x86_64)
            in the session ${event.requestContext!!.sessionReference}
            """.trimIndent()
        assertTrue(output.out.lines().joinToString("\n").contains("\n" + message + "\n"), output.out)
    }

    @Test
    fun `a user agent cannot forge log lines and is capped`(output: CapturedOutput) {
        val forged = "curl/8.0\r\nThe user admin@domain.com\nhas done an AuthenticationSuccessEvent event"
        val event = anEventCreatedInARequest(userAgent = forged + "x".repeat(600))

        underTest.accept(event)

        val loggedUserAgent = output.out.lines().single { it.startsWith("with the user agent ") }
            .removePrefix("with the user agent ")
        assertEquals(
            "curl/8.0\\r\\nThe user admin@domain.com\\nhas done an AuthenticationSuccessEvent event" + "x".repeat(512 - forged.length),
            loggedUserAgent
        )
        assertTrue(output.out.lines().none { it.startsWith("The user admin@domain.com") })
    }

    @Test
    fun `a password carried by an event is never logged`(output: CapturedOutput) {
        val event = SignUpEvent(
            Email("anemail@domain.com"),
            ClientAppId(A_CLIENT_APP_ID),
            Instant.now(),
            Password("\$2a\$12\$AN_ENCODED_PASSWORD")
        )

        underTest.accept(event)

        assertTrue(output.out.lines().any { it == "event payload: Password(*****)" }, output.out)
        assertTrue(!output.out.contains("AN_ENCODED_PASSWORD"), output.out)
    }

    @Test
    fun `a username cannot forge log lines`(output: CapturedOutput) {
        val event = SignUpEvent(
            Email("attacker\r\nThe user admin@domain.com"),
            ClientAppId(A_CLIENT_APP_ID),
            Instant.now(),
            Password("A_PASSWORD")
        )

        underTest.accept(event)

        assertTrue(output.out.contains("The user attacker\\r\\nThe user admin@domain.com\n"), output.out)
        assertTrue(output.out.lines().none { it.startsWith("The user admin@domain.com") }, output.out)
    }

    @Test
    fun `a username inside an authentication event payload cannot forge log lines`(output: CapturedOutput) {
        val forgedUserName = "attacker\nThe user admin@domain.com"
        val event = VAuthenticatorAuthEvent(
            Email(forgedUserName),
            ClientAppId(A_CLIENT_APP_ID),
            Instant.now(),
            AuthenticationFailureBadCredentialsEvent(
                UsernamePasswordAuthenticationToken(forgedUserName, "*******"),
                BadCredentialsException("bad credentials")
            )
        )

        underTest.accept(event)

        assertTrue(output.out.lines().none { it.startsWith("The user admin@domain.com") }, output.out)
    }

    @Test
    fun `the session id inside an authentication event payload is never logged`(output: CapturedOutput) {
        val request = MockHttpServletRequest().apply {
            remoteAddr = "203.0.113.7"
            setSession(MockHttpSession(null, "A_RAW_SESSION_ID"))
        }
        val token = UsernamePasswordAuthenticationToken("anemail@domain.com", "*******")
            .apply { details = WebAuthenticationDetails(request) }
        val event = VAuthenticatorAuthEvent(
            Email("anemail@domain.com"),
            ClientAppId(A_CLIENT_APP_ID),
            Instant.now(),
            AuthenticationFailureBadCredentialsEvent(token, BadCredentialsException("bad credentials"))
        )

        underTest.accept(event)

        assertTrue(
            output.out.lines().any {
                it == "event payload: AuthenticationFailureBadCredentialsEvent(BadCredentialsException)"
            },
            output.out
        )
        assertTrue(!output.out.contains("A_RAW_SESSION_ID"), output.out)
    }

    @Test
    fun `a client id cannot forge log lines`(output: CapturedOutput) {
        val event = SignUpEvent(
            Email("anemail@domain.com"),
            ClientAppId("x\nThe user admin@domain.com"),
            Instant.now(),
            Password("A_PASSWORD")
        )

        underTest.accept(event)

        assertTrue(output.out.lines().any { it == "with the client id x\\nThe user admin@domain.com" }, output.out)
    }

    @Test
    fun `an event without a client app is logged with no client id`(output: CapturedOutput) {
        val event = SignUpEvent(Email("anemail@domain.com"), ClientAppId.empty(), Instant.now(), Password("A_PASSWORD"))

        underTest.accept(event)

        assertTrue(output.out.lines().any { it == "with the client id none" }, output.out)
    }

    @Test
    fun `unicode line separators and control characters cannot forge log lines`(output: CapturedOutput) {
        val event = SignUpEvent(
            Email("a\u0085b c d\u001B[2Je"),
            ClientAppId(A_CLIENT_APP_ID),
            Instant.now(),
            Password("A_PASSWORD")
        )

        underTest.accept(event)

        assertTrue(output.out.contains("The user a\\u0085b\\u2028c\\u2029d\\u001b[2Je\n"), output.out)
    }

    @Test
    fun `the user agent is capped before it is escaped so no escape is cut in half`(output: CapturedOutput) {
        val event = anEventCreatedInARequest(userAgent = "a".repeat(511) + "\r" + "tail")

        underTest.accept(event)

        assertTrue(output.out.lines().any { it == "with the user agent " + "a".repeat(511) + "\\r" }, output.out)
    }

    @Test
    fun `an ip address taken from a forwarded header cannot forge log lines`(output: CapturedOutput) {
        val event = anEventCreatedInARequest(userAgent = "curl/8.0", remoteAddr = "1.2.3.4\nThe user admin@domain.com")

        underTest.accept(event)

        assertTrue(output.out.lines().any { it == "from the ip address 1.2.3.4\\nThe user admin@domain.com" }, output.out)
    }

    private val underTest = LoggerEventConsumer(EventConsumerConfig(mapOf("logger-event-consumer" to true)))

    private fun anEventCreatedInARequest(userAgent: String, remoteAddr: String = "203.0.113.7"): VAuthenticatorEvent {
        val request = MockHttpServletRequest().apply {
            this.remoteAddr = remoteAddr
            addHeader("User-Agent", userAgent)
            setSession(MockHttpSession(null, "A_SESSION_ID"))
        }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        try {
            return SignUpEvent(Email("anemail@domain.com"), ClientAppId(A_CLIENT_APP_ID), Instant.now(), Password("A_PASSWORD"))
        } finally {
            RequestContextHolder.resetRequestAttributes()
        }
    }
}