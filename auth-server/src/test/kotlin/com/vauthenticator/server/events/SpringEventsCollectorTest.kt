package com.vauthenticator.server.events

import com.vauthenticator.server.events.EventFixture.vauthenticatorAuthEvent
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SpringEventsCollectorTest {

    @MockK
    private lateinit var eventConsumer: EventConsumer

    @MockK
    private lateinit var anotherEventConsumer: EventConsumer

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
}