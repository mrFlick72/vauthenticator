package com.vauthenticator.platform.events

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Instant

/**
 * Something that happened in a server, with the request it happened in ([RequestContext], ADR 0016 of the auth
 * server). A server defines its own events on top of it, usually as a sealed hierarchy, and narrows the contracts
 * below to that hierarchy.
 */
abstract class Event(
    val timeStamp: Instant,
    val payload: Any,
    val requestContext: RequestContext? = RequestContext.current()
)

interface EventsDispatcher<in E : Event> {
    fun dispatch(event: E)
}

interface EventsCollector<in E : Event> {
    fun accept(event: E)
}

interface EventConsumer<in E : Event> {
    fun accept(event: E)
    fun handleable(event: E): Boolean
}

/** The consumers a server switches on by name: `event.consumer.enable.<name>=true`. */
@ConfigurationProperties("event.consumer")
data class EventConsumerConfig(val enable: Map<String, Boolean>)
