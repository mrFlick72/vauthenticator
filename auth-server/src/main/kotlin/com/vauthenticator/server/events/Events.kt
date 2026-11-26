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
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, payload)

class SignUpEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    password : Password
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, password)

class ChangePasswordEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    password : Password
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, password)

class PasswordLifeCycleActionAppliedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    action: PasswordLifeCycleAction
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, action)

class FailedLoginLockAppliedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    applied: FailedLoginLockApplied
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, applied)

class FailedLoginLockReleasedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    release: FailedLoginLockRelease
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, release)

class ResetPasswordEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    password : Password
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, password)

class MfaDeviceRevokedEvent(
    userName: Email,
    clientAppId: ClientAppId,
    timeStamp: Instant,
    mfaDeviceRevoked: MfaDeviceRevoked
) : VAuthenticatorEvent(userName.content, clientAppId.content, timeStamp, mfaDeviceRevoked)
