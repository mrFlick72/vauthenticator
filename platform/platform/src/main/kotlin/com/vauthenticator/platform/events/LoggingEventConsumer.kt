package com.vauthenticator.platform.events

import com.vauthenticator.platform.extensions.escapedForLog
import org.slf4j.LoggerFactory
import org.springframework.util.ClassUtils

/** The name that switches the consumer on: `event.consumer.enable.logger-event-consumer=true`. */
const val LOGGER_EVENT_CONSUMER = "logger-event-consumer"
private const val MAX_LOGGED_VALUE_LENGTH = 512
private const val SPRING_AUTHENTICATION_EVENT = "org.springframework.security.authentication.event.AbstractAuthenticationEvent"

/**
 * Writes every event to the log. Every client-controlled value is escaped ([escapedForLog]) so it can neither forge nor
 * disguise log lines; values with no natural bound are capped first. A [SensitivePayload] is written as its type
 * only, and so is a Spring Security authentication event, whose text holds the typed username and the raw session id.
 */
open class LoggingEventConsumer<in E : VAuthenticatorEvent>(private val eventConsumerConfig: EventConsumerConfig) :
    EventConsumer<E> {

    private val logger = LoggerFactory.getLogger(LoggingEventConsumer::class.java)
    private val springSecurityPresent = ClassUtils.isPresent(SPRING_AUTHENTICATION_EVENT, javaClass.classLoader)

    override fun accept(event: E) {
        val logLine = """
            The user ${event.principal.escapedForLog()}
            with the client id ${event.clientId.ifEmpty { "none" }.escapedForLog()}
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

    private fun loggablePayloadOf(event: VAuthenticatorEvent): String {
        val payload = event.payload
        if (payload is SensitivePayload) return "${payload::class.simpleName}(*****)"
        if (springSecurityPresent) AuthenticationPayloads.typeOnly(payload)?.let { return it }
        return payload.toString().escapedForLog()
    }

    /** Client-controlled values with no natural bound (user agent, forwarded IP) are capped, never mid-character, then escaped. */
    private fun String.cappedForLog(): String {
        val capped = take(MAX_LOGGED_VALUE_LENGTH)
        return (if (capped.lastOrNull()?.isHighSurrogate() == true) capped.dropLast(1) else capped).escapedForLog()
    }

    override fun handleable(event: E) = eventConsumerConfig.enable[LOGGER_EVENT_CONSUMER] ?: false
}

/** Kept apart so that the Spring Security classes are only loaded when they are on the classpath. */
private object AuthenticationPayloads {
    fun typeOnly(payload: Any): String? =
        when (payload) {
            is org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent ->
                "${payload::class.simpleName}(${payload.exception::class.simpleName})"

            is org.springframework.security.authentication.event.AbstractAuthenticationEvent ->
                "${payload::class.simpleName}"

            else -> null
        }
}
