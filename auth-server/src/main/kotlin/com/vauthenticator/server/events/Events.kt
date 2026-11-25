package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockApplied
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockRelease
import com.vauthenticator.server.mfa.domain.MfaDeviceRevoked
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.platform.events.Event
import com.vauthenticator.platform.events.RequestContext
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import java.time.Instant

interface EventsDispatcher : com.vauthenticator.platform.events.EventsDispatcher<VAuthenticatorEvent>

interface EventsCollector : com.vauthenticator.platform.events.EventsCollector<VAuthenticatorEvent>

interface EventConsumer : com.vauthenticator.platform.events.EventConsumer<VAuthenticatorEvent>

/**
 * The events of the authorization server, on top of the platform [Event]: the account and the client app stay typed
 * here, and the platform gets their values as its principal and client id.
 */
sealed class VAuthenticatorEvent(
    val userName: Email,
    val clientAppId: ClientAppId,
    timeStamp: Instant,
    payload: Any,
    requestContext: RequestContext? = RequestContext.current()
) : Event(userName.content, clientAppId.content, timeStamp, payload, requestContext)


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

class PasswordLifeCycleActionAppliedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    action: PasswordLifeCycleAction
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, action) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class FailedLoginLockAppliedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    applied: FailedLoginLockApplied
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, applied) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class FailedLoginLockReleasedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    release: FailedLoginLockRelease
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, release) {
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

class MfaDeviceRevokedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    mfaDeviceRevoked: MfaDeviceRevoked
) : VAuthenticatorEvent(userName, clientAppId, timeStamp, mfaDeviceRevoked) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
