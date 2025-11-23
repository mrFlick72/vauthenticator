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
import org.springframework.context.ApplicationEventPublisher

@ExtendWith(MockKExtension::class)
class VAuthenticatorEventsDispatcherTest {

   @MockK
    private lateinit var publisher: ApplicationEventPublisher

    @Test
    fun `when an event is published`() {
        val underTest = VAuthenticatorEventsDispatcher(publisher)

        every { publisher.publishEvent(vauthenticatorAuthEvent) } just runs

        underTest.dispatch(vauthenticatorAuthEvent)

        verify { publisher.publishEvent(vauthenticatorAuthEvent) }
    }
}