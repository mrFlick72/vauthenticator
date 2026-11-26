package com.vauthenticator.platform.events

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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

    private class Collector(consumers: List<EventConsumer<AnEvent>>) : ConsumingEventsCollector<AnEvent>(consumers)

    @Test
    fun `an event goes to every consumer that can handle it`() {
        val handling = RecordingConsumer(handles = true)
        val anotherHandling = RecordingConsumer(handles = true)
        val notHandling = RecordingConsumer(handles = false)
        val event = AnEvent("a payload")

        Collector(listOf(handling, notHandling, anotherHandling)).accept(event)

        assertEquals(listOf(event), handling.accepted)
        assertEquals(listOf(event), anotherHandling.accepted)
        assertEquals(emptyList<AnEvent>(), notHandling.accepted)
    }
}
