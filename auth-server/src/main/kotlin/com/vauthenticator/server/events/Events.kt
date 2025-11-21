package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.password.domain.Password
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import java.time.Instant

interface EventsDispatcher {
    fun dispatch(event: VAuthenticatorEvent)
}

interface EventsCollector {
    fun accept(event: VAuthenticatorEvent)

}

interface EventConsumer {
    fun accept(event: VAuthenticatorEvent)
    fun handleable(event: VAuthenticatorEvent): Boolean

}

@ConfigurationProperties("event.consumer")
data class EventConsumerConfig(val enable: Map<String, Boolean>)

sealed class VAuthenticatorEvent(
    val userName: Email,
    val clientAppId: ClientAppId,
    val timeStamp: Instant,
    val payload: Any
)


class VAuthenticatorAuthEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    payload: AbstractAuthenticationEvent
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, payload) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class SignUpEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    password : Password
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, password) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class ChangePasswordEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    password : Password
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, password) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class ResetPasswordEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    password : Password
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, password) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
