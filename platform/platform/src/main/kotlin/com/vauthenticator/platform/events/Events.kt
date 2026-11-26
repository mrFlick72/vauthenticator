package com.vauthenticator.platform.events

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Instant

/**
 * Something that happened in a VAuthenticator server: who it happened to ([principal], such as an account's username),
 * through which client ([clientId], empty when there is none), when, with which [payload], and the request it happened
 * in ([RequestContext], ADR 0016 of the auth server). Every server's events extend it directly, each server in its own
 * process; a server can still narrow the contracts below to a subtype of its own.
 */
abstract class VAuthenticatorEvent(
    val principal: String,
    val clientId: String,
    val timeStamp: Instant,
    val payload: Any,
    val requestContext: RequestContext? = RequestContext.current()
)

/** A payload that must never be written anywhere, such as a password, even encoded: it is logged as its type only. */
interface SensitivePayload

interface EventsDispatcher<in E : VAuthenticatorEvent> {
    fun dispatch(event: E)
}

interface EventsCollector<in E : VAuthenticatorEvent> {
    fun accept(event: E)
}

interface EventConsumer<in E : VAuthenticatorEvent> {
    fun accept(event: E)
    fun handleable(event: E): Boolean
}

/** The consumers a server switches on by name: `event.consumer.enable.<name>=true`. */
@ConfigurationProperties("event.consumer")
data class EventConsumerConfig(val enable: Map<String, Boolean>)
