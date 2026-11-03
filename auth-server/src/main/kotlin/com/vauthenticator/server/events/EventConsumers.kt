package com.vauthenticator.server.events

import com.vauthenticator.server.password.domain.Password
import org.slf4j.LoggerFactory

private const val LOGGER_EVENT_CONSUMER = "logger-event-consumer"
private const val MAX_LOGGED_USER_AGENT_LENGTH = 512

class LoggerEventConsumer(private val eventConsumerConfig: EventConsumerConfig) : EventConsumer {

    private val logger = LoggerFactory.getLogger(LoggerEventConsumer::class.java)

    override fun accept(event: VAuthenticatorEvent) {
        val logLine = """
            The user ${escapeLineBreaks(event.userName.content)}
            with the client id ${event.clientAppId.content}
            has done an ${event.payload::class.simpleName} event
            event at ${event.timeStamp.epochSecond}
            event payload: ${loggablePayloadOf(event)}
     """.trimIndent()
        logger.info(logLine + requestContextLinesOf(event))
    }

    private fun requestContextLinesOf(event: VAuthenticatorEvent): String =
        event.requestContext?.let {
            """

            from the ip address ${it.ipAddress}
            with the user agent ${it.userAgent?.let { userAgent -> escapeLineBreaks(userAgent).take(MAX_LOGGED_USER_AGENT_LENGTH) } ?: "none"}
            in the session ${it.sessionReference ?: "none"}
            """.trimIndent()
        } ?: ""

    /** A password, even encoded, is never written to the log. */
    private fun loggablePayloadOf(event: VAuthenticatorEvent): String =
        when (event.payload) {
            is Password -> "Password(*****)"
            else -> escapeLineBreaks(event.payload.toString())
        }

    /** Usernames (also inside an authentication payload) and user agents are client-controlled: escaped line breaks cannot forge log lines. */
    private fun escapeLineBreaks(value: String) =
        value.replace("\r", "\\r").replace("\n", "\\n")

    override fun handleable(event: VAuthenticatorEvent) = eventConsumerConfig.enable[LOGGER_EVENT_CONSUMER] ?: false
}