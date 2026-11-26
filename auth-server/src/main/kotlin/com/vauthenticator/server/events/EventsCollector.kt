package com.vauthenticator.server.events

import com.vauthenticator.platform.events.EventConsumer
import com.vauthenticator.platform.events.VAuthenticatorEvent
import com.vauthenticator.platform.events.ConsumingEventsCollector
import org.springframework.context.event.EventListener

class SpringEventsCollector(eventConsumers: List<EventConsumer<VAuthenticatorEvent>>) :
    ConsumingEventsCollector<VAuthenticatorEvent>(eventConsumers) {

    @EventListener
    override fun accept(event: VAuthenticatorEvent) = super.accept(event)
}
