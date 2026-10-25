package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.extentions.loginClientAppId
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import org.springframework.context.event.EventListener
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent

/**
 * Feeds password logins into the [FailedLoginLock]. MFA and change-password publish their own subclasses of the
 * Spring authentication events with the same username/password token, so only the exact event classes count.
 */
class FailedLoginLockAuthenticationListener(
    private val failedLoginLock: FailedLoginLock,
    private val currentHttpServletRequestService: CurrentHttpServletRequestService
) {

    @EventListener
    fun onFailure(event: AbstractAuthenticationFailureEvent) {
        if (event.javaClass == AuthenticationFailureBadCredentialsEvent::class.java && event.isPasswordLogin()) {
            failedLoginLock.failedLogin(
                event.authentication.name,
                currentHttpServletRequestService.getServletRequest().loginClientAppId()
            )
        }
    }

    @EventListener
    fun onSuccess(event: AuthenticationSuccessEvent) {
        if (event.javaClass == AuthenticationSuccessEvent::class.java && event.isPasswordLogin()) {
            failedLoginLock.passwordProven(event.authentication.name)
        }
    }

    private fun AbstractAuthenticationEvent.isPasswordLogin() =
        authentication is UsernamePasswordAuthenticationToken
}
