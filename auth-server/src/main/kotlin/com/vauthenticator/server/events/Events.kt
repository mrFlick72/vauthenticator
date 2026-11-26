package com.vauthenticator.server.events

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockApplied
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockRelease
import com.vauthenticator.server.mfa.domain.MfaDeviceRevoked
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.platform.events.VAuthenticatorEvent
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import java.time.Instant

class VAuthenticatorAuthEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    payload: AbstractAuthenticationEvent
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, payload) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, password) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, password) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, action) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, applied) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, release) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, password) {
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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, mfaDeviceRevoked) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
