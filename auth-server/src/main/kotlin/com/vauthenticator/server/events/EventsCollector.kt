package com.vauthenticator.server.events

import com.vauthenticator.platform.events.ConsumingEventsCollector
import org.springframework.context.event.EventListener

class SpringEventsCollector(eventConsumers: List<EventConsumer>) :
    ConsumingEventsCollector<VAuthenticatorEvent>(eventConsumers), EventsCollector {

    @EventListener
    override fun accept(event: VAuthenticatorEvent) = super.accept(event)
}
