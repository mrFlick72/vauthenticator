package com.vauthenticator.server.events

import com.vauthenticator.server.extentions.escapedForLog
import com.vauthenticator.server.password.domain.Password
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent

private const val LOGGER_EVENT_CONSUMER = "logger-event-consumer"
private const val MAX_LOGGED_VALUE_LENGTH = 512

class LoggerEventConsumer(private val eventConsumerConfig: EventConsumerConfig) : EventConsumer {

    private val logger = LoggerFactory.getLogger(LoggerEventConsumer::class.java)

    override fun accept(event: VAuthenticatorEvent) {
        val logLine = """
            The user ${event.userName.content.escapedForLog()}
            with the client id ${event.clientAppId.content.ifEmpty { "none" }.escapedForLog()}
            has done an ${event.payload::class.simpleName} event
            event at ${event.timeStamp.epochSecond}
            event payload: ${loggablePayloadOf(event)}
     """.trimIndent()
        logger.info(logLine + requestContextLinesOf(event))
    }

    private fun requestContextLinesOf(event: VAuthenticatorEvent): String =
        event.requestContext?.let {
            """

            from the ip address ${it.ipAddress.cappedForLog()}
            with the user agent ${it.userAgent?.cappedForLog() ?: "none"}
            in the session ${it.sessionReference ?: "none"}
            """.trimIndent()
        } ?: ""

    /**
     * A password, even encoded, is never written to the log. An authentication payload is written as its type only:
     * its text holds the typed username and the raw session id (in the request details).
     */
    private fun loggablePayloadOf(event: VAuthenticatorEvent): String =
        when (val payload = event.payload) {
            is Password -> "Password(*****)"
            is AbstractAuthenticationFailureEvent ->
                "${payload::class.simpleName}(${payload.exception::class.simpleName})"
            is AbstractAuthenticationEvent -> "${payload::class.simpleName}"
            else -> payload.toString().escapedForLog()
        }

    /** Client-controlled values with no natural bound (user agent, forwarded IP) are capped, never mid-character, then escaped. */
    private fun String.cappedForLog(): String {
        val capped = take(MAX_LOGGED_VALUE_LENGTH)
        return (if (capped.lastOrNull()?.isHighSurrogate() == true) capped.dropLast(1) else capped).escapedForLog()
    }

    override fun handleable(event: VAuthenticatorEvent) = eventConsumerConfig.enable[LOGGER_EVENT_CONSUMER] ?: false
}