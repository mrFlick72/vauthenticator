package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.extentions.oauth2ClientId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant
import java.util.*

class VAuthenticatorEventsDispatcher(private val publisher: ApplicationEventPublisher) : EventsDispatcher {
    override fun dispatch(event: VAuthenticatorEvent) {
        publisher.publishEvent(event)
    }

}

class SpringEventEventsDispatcher(private val publisher: ApplicationEventPublisher) : EventsDispatcher {

    private val logger = LoggerFactory.getLogger(SpringEventEventsDispatcher::class.java)

    @EventListener
    fun handle(event: AbstractAuthenticationEvent) {
        val currentRequest = httpServletRequestFromRequestContextHolder()
        clientIdForm(currentRequest)
            .ifPresentOrElse(
                { dispatchAdaptedEventFor(it.content, event) },
                { logger.debug("PRE EVENT NOT PROCESSED") }
            )
    }

    private fun httpServletRequestFromRequestContextHolder(): HttpServletRequest {
        logger.debug("PRE EVENT PROCESSING")
        return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
    }

    private fun clientIdForm(currentRequest: HttpServletRequest): Optional<ClientAppId> =
        currentRequest.oauth2ClientId().or { currentRequest.session.oauth2ClientId() }

    private fun dispatchAdaptedEventFor(
        it: String,
        event: AbstractAuthenticationEvent
    ) {
        logger.debug("EVENT PROCESSING")

        dispatch(
            VAuthenticatorAuthEvent(
                Email((Optional.ofNullable(event.authentication.name)).orElseGet { "UNKNOWN" }),
                ClientAppId(it),
                Instant.now(),
                event
            )
        )
    }

    override fun dispatch(event: VAuthenticatorEvent) {
        publisher.publishEvent(event)
    }
}
