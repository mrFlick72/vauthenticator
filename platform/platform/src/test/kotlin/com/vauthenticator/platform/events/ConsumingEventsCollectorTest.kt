package com.vauthenticator.platform.events

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigUtils
import org.springframework.context.support.GenericApplicationContext
import java.time.Instant

class ConsumingEventsCollectorTest {

    private class AnEvent(payload: String) : VAuthenticatorEvent("a user", "a client", Instant.EPOCH, payload, null)

    private class RecordingConsumer(private val handles: Boolean) : EventConsumer<AnEvent> {
        val accepted = mutableListOf<AnEvent>()
        override fun accept(event: AnEvent) {
            accepted.add(event)
        }

        override fun handleable(event: AnEvent) = handles
    }

    @Test
    fun `an event goes to every consumer that can handle it`() {
        val handling = RecordingConsumer(handles = true)
        val anotherHandling = RecordingConsumer(handles = true)
        val notHandling = RecordingConsumer(handles = false)
        val event = AnEvent("a payload")

        ConsumingEventsCollector(listOf(handling, notHandling, anotherHandling)).accept(event)

        assertEquals(listOf(event), handling.accepted)
        assertEquals(listOf(event), anotherHandling.accepted)
        assertEquals(emptyList<AnEvent>(), notHandling.accepted)
    }

    @Test
    fun `as the event listener it receives VAuthenticator events only`() {
        val consumer = RecordingConsumer(handles = true)
        val event = AnEvent("a payload")
        val anApplicationEvent = "an application event that is not a VAuthenticator event"

        GenericApplicationContext().use { context ->
            context.registerBean(ConsumingEventsCollector::class.java, listOf(consumer))
            // the @EventListener support a Spring Boot context has
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context)
            context.refresh()

            context.publishEvent(anApplicationEvent)
            context.publishEvent(event)
        }

        assertEquals(listOf(event), consumer.accepted)
    }
}
