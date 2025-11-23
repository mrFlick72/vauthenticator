package com.vauthenticator.server.events

import org.slf4j.LoggerFactory

private const val LOGGER_EVENT_CONSUMER = "logger-event-consumer"

class LoggerEventConsumer(private val eventConsumerConfig: EventConsumerConfig) : EventConsumer {

    private val logger = LoggerFactory.getLogger(LoggerEventConsumer::class.java)

    override fun accept(event: VAuthenticatorEvent) {
        val logLine = """
            The user ${event.userName.content}
            with the client id ${event.clientAppId.content}
            has done an ${event.payload::class.simpleName} event
            event at ${event.timeStamp.epochSecond}
            event payload: ${event.payload}
     """.trimIndent()
        logger.info(logLine)

    }

    override fun handleable(event: VAuthenticatorEvent) = eventConsumerConfig.enable[LOGGER_EVENT_CONSUMER] ?: false
}