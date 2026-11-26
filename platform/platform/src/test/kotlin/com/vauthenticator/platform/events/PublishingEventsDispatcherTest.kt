package com.vauthenticator.platform.events

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.Instant

class PublishingEventsDispatcherTest {

    @Test
    fun `an event is dispatched as a Spring application event`() {
        val publisher = mockk<ApplicationEventPublisher>()
        val event = object : VAuthenticatorEvent("a user", "a client", Instant.EPOCH, "a payload", null) {}
        every { publisher.publishEvent(event) } just runs

        PublishingEventsDispatcher<VAuthenticatorEvent>(publisher).dispatch(event)

        verify { publisher.publishEvent(event) }
    }
}
