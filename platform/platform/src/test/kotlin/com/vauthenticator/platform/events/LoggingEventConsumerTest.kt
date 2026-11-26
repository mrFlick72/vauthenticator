package com.vauthenticator.platform.events

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import java.time.Instant

@ExtendWith(OutputCaptureExtension::class)
class LoggingEventConsumerTest {

    private class AnEvent(principal: String, clientId: String, payload: Any, requestContext: RequestContext? = null) :
        VAuthenticatorEvent(principal, clientId, Instant.ofEpochSecond(42), payload, requestContext)

    private class ASecret(val value: String) : SensitivePayload {
        override fun toString() = value
    }

    private val underTest = LoggingEventConsumer<AnEvent>(EventConsumerConfig(mapOf(LOGGER_EVENT_CONSUMER to true)))

    @Test
    fun `an event is logged with who, through which client, when and its request context`(output: CapturedOutput) {
        underTest.accept(AnEvent("user@domain.com", "A_CLIENT", "a payload", RequestContext("10.0.0.1", "a browser", "A_HASH")))

        assertTrue(output.out.contains("The user user@domain.com"))
        assertTrue(output.out.contains("with the client id A_CLIENT"))
        assertTrue(output.out.contains("event at 42"))
        assertTrue(output.out.contains("event payload: a payload"))
        assertTrue(output.out.contains("from the ip address 10.0.0.1"))
        assertTrue(output.out.contains("in the session A_HASH"))
    }

    @Test
    fun `an event without a client is logged with no client id`(output: CapturedOutput) {
        underTest.accept(AnEvent("user@domain.com", "", "a payload"))

        assertTrue(output.out.contains("with the client id none"))
    }

    @Test
    fun `a sensitive payload is logged as its type only`(output: CapturedOutput) {
        underTest.accept(AnEvent("user@domain.com", "A_CLIENT", ASecret("A_SECRET_VALUE")))

        assertFalse(output.out.contains("A_SECRET_VALUE"))
        assertTrue(output.out.contains("event payload: ASecret(*****)"))
    }

    @Test
    fun `an authentication event payload is logged as its type only`(output: CapturedOutput) {
        val authentication = UsernamePasswordAuthenticationToken("A_TYPED_USERNAME", "A_TYPED_PASSWORD")

        underTest.accept(AnEvent("user@domain.com", "A_CLIENT", AuthenticationSuccessEvent(authentication)))

        assertFalse(output.out.contains("A_TYPED_PASSWORD"))
        assertTrue(output.out.contains("event payload: AuthenticationSuccessEvent"))
    }

    @Test
    fun `a principal or a client id cannot forge log lines`(output: CapturedOutput) {
        underTest.accept(AnEvent("x\nThe user admin@domain.com", "A_CLIENT\r\nforged", "a payload"))

        assertFalse(output.out.contains("\nThe user admin@domain.com"))
        assertTrue(output.out.contains("x\\nThe user admin@domain.com"))
        assertTrue(output.out.contains("A_CLIENT\\r\\nforged"))
    }

    @Test
    fun `it logs only when switched on`() {
        val switchedOff = LoggingEventConsumer<AnEvent>(EventConsumerConfig(emptyMap()))

        assertFalse(switchedOff.handleable(AnEvent("user@domain.com", "A_CLIENT", "a payload")))
        assertTrue(underTest.handleable(AnEvent("user@domain.com", "A_CLIENT", "a payload")))
    }
}
