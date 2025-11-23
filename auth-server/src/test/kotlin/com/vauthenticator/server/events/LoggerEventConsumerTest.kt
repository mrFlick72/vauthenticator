package com.vauthenticator.server.events

import com.vauthenticator.server.events.EventFixture.vauthenticatorAuthEvent
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

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
            event payload: org.springframework.security.authentication.event.AuthenticationSuccessEvent
            """.trimIndent()
        assertTrue(output.out.contains(message))
    }

}