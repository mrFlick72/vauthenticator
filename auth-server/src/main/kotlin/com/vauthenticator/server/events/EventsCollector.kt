package com.vauthenticator.server.events

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener

class SpringEventsCollector(private val eventConsumers: List<EventConsumer>) : EventsCollector {

    private val logger = LoggerFactory.getLogger(SpringEventsCollector::class.java)
    @EventListener
    override fun accept(event: VAuthenticatorEvent) {
        logger.debug("event ${event::class.simpleName} is collected")
        eventConsumers.forEach {
            val handleable = it.handleable(event)
            logger.debug("event ${event::class.simpleName} is handleable $handleable")
            if(handleable){
                it.accept(event)
            }
        }
    }
}