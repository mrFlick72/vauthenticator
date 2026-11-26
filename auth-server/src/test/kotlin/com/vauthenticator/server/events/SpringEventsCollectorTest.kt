package com.vauthenticator.server.events

import com.vauthenticator.platform.events.EventConsumer
import com.vauthenticator.platform.events.VAuthenticatorEvent
import com.vauthenticator.server.events.EventFixture.vauthenticatorAuthEvent
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.AnnotationConfigUtils
import org.springframework.context.support.GenericApplicationContext

@ExtendWith(MockKExtension::class)
class SpringEventsCollectorTest {

    @MockK
    private lateinit var eventConsumer: EventConsumer<VAuthenticatorEvent>

    @MockK
    private lateinit var anotherEventConsumer: EventConsumer<VAuthenticatorEvent>

    @Test
    fun `when an event is received and processed from a list of event consumers`() {
        val underTest = SpringEventsCollector(listOf(eventConsumer, anotherEventConsumer))

        every { eventConsumer.accept(vauthenticatorAuthEvent) } just runs
        every { eventConsumer.handleable(vauthenticatorAuthEvent) } returns true
        every { anotherEventConsumer.accept(vauthenticatorAuthEvent) } just runs
        every { anotherEventConsumer.handleable(vauthenticatorAuthEvent) } returns true

        underTest.accept(vauthenticatorAuthEvent)

        verify { eventConsumer.accept(vauthenticatorAuthEvent) }
        verify { anotherEventConsumer.accept(vauthenticatorAuthEvent) }
        verify { anotherEventConsumer.accept(vauthenticatorAuthEvent) }
        verify { anotherEventConsumer.handleable(vauthenticatorAuthEvent) }
    }

    @Test
    fun `as the event listener it receives VAuthenticator events only`() {
        val received = mutableListOf<Any>()
        val recordingConsumer = object : EventConsumer<VAuthenticatorEvent> {
            override fun accept(event: VAuthenticatorEvent) {
                received.add(event)
            }

            override fun handleable(event: VAuthenticatorEvent) = true
        }
        val anApplicationEvent = "an application event that is not a VAuthenticator event"

        GenericApplicationContext().use { context ->
            context.registerBean(SpringEventsCollector::class.java, listOf(recordingConsumer))
            // the @EventListener support a Spring Boot context has
            AnnotationConfigUtils.registerAnnotationConfigProcessors(context)
            context.refresh()

            context.publishEvent(anApplicationEvent)
            context.publishEvent(vauthenticatorAuthEvent)
        }

        assertEquals(listOf<Any>(vauthenticatorAuthEvent), received)
    }
}
