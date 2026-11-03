package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.extentions.loginClientAppId
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant

class VAuthenticatorEventsDispatcher(private val publisher: ApplicationEventPublisher) : EventsDispatcher {
    override fun dispatch(event: VAuthenticatorEvent) {
        publisher.publishEvent(event)
    }

}

class SpringEventEventsDispatcher(private val publisher: ApplicationEventPublisher) : EventsDispatcher {

    private val logger = LoggerFactory.getLogger(SpringEventEventsDispatcher::class.java)

    /** Wraps a Spring authentication event; one without a client app is attributed to the System Client. */
    @EventListener
    fun handle(event: AbstractAuthenticationEvent) {
        logger.debug("EVENT PROCESSING")
        dispatch(
            VAuthenticatorAuthEvent(
                Email(event.authentication.name ?: "UNKNOWN"),
                httpServletRequestFromRequestContextHolder().loginClientAppId(),
                Instant.now(),
                event
            )
        )
    }

    private fun httpServletRequestFromRequestContextHolder(): HttpServletRequest =
        (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

    override fun dispatch(event: VAuthenticatorEvent) {
        publisher.publishEvent(event)
    }
}
