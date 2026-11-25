package com.vauthenticator.platform.events

import org.springframework.context.ApplicationEventPublisher

/** Dispatches an event as a Spring application event, so that the server's collector receives it in process. */
open class PublishingEventsDispatcher<in E : Event>(private val publisher: ApplicationEventPublisher) :
    EventsDispatcher<E> {

    override fun dispatch(event: E) {
        publisher.publishEvent(event)
    }
}
