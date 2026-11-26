package com.vauthenticator.platform.events

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener

/**
 * Hands every collected event to each consumer that can handle it, in the order of the list. Declared as a bean, it is
 * the event listener of the pipeline: it receives every [VAuthenticatorEvent] published in the application context and
 * no other application event. Spring resolves the listened type from [accept]'s declared parameter, so the collector
 * always listens for [VAuthenticatorEvent], whatever [E] a server binds it to.
 */
open class ConsumingEventsCollector<E : VAuthenticatorEvent>(private val eventConsumers: List<EventConsumer<E>>) :
    EventsCollector<E> {

    private val logger = LoggerFactory.getLogger(ConsumingEventsCollector::class.java)

    @EventListener
    override fun accept(event: E) {
        logger.debug("event ${event::class.simpleName} is collected")
        eventConsumers.forEach {
            val handleable = it.handleable(event)
            logger.debug("event ${event::class.simpleName} is handleable $handleable")
            if (handleable) {
                it.accept(event)
            }
        }
    }
}
