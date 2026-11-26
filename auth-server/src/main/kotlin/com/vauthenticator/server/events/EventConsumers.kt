package com.vauthenticator.server.events

import com.vauthenticator.platform.events.VAuthenticatorEvent
import com.vauthenticator.platform.events.EventConsumerConfig
import com.vauthenticator.platform.events.LoggingEventConsumer

/** The platform's event logger, bound to the authorization server events; a [com.vauthenticator.server.password.domain.Password] payload is a sensitive one. */
class LoggerEventConsumer(eventConsumerConfig: EventConsumerConfig) :
    LoggingEventConsumer<VAuthenticatorEvent>(eventConsumerConfig)
