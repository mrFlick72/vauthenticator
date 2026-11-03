package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.extentions.oauth2ClientId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
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

    /**
     * Wraps a Spring authentication event. Without a client app, only a login (a username/password authentication,
     * which the MFA and change-password events also carry) is dispatched, with no client app ([ClientAppId.empty]);
     * other authentications without one, such as bearer tokens on API calls, are not.
     */
    @EventListener
    fun handle(event: AbstractAuthenticationEvent) {
        val clientAppId = currentRequest()?.let(::clientAppIdOf)
            ?: if (event.authentication is UsernamePasswordAuthenticationToken) ClientAppId.empty() else null
        if (clientAppId == null) {
            logger.debug("EVENT NOT PROCESSED: no client app and not a login")
            return
        }
        dispatch(
            VAuthenticatorAuthEvent(
                Email(event.authentication.name ?: "UNKNOWN"),
                clientAppId,
                Instant.now(),
                event
            )
        )
    }

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    private fun clientAppIdOf(request: HttpServletRequest): ClientAppId? =
        request.oauth2ClientId() ?: request.getSession(false)?.oauth2ClientId()

    override fun dispatch(event: VAuthenticatorEvent) {
        publisher.publishEvent(event)
    }
}
