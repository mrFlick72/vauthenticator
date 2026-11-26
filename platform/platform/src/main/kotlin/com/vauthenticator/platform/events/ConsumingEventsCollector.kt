package com.vauthenticator.platform.events

import org.slf4j.LoggerFactory

/**
 * Hands every collected event to each consumer that can handle it, in the order of the list. A server subclasses it
 * for its own event hierarchy and marks its `accept` override as the event listener, so that it only receives the
 * events of that hierarchy.
 */
abstract class ConsumingEventsCollector<E : VAuthenticatorEvent>(private val eventConsumers: List<EventConsumer<E>>) :
    EventsCollector<E> {

    private val logger = LoggerFactory.getLogger(ConsumingEventsCollector::class.java)

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
