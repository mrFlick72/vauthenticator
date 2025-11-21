package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.keys.domain.Kid
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

data class MfaDevice(
    val userName: String,
    val mfaMethod: MfaMethod,
    val mfaChannel: String,
    val mfaDeviceId: MfaDeviceId,
    val default: Boolean
)

data class MfaDeviceId(val content: String)

class MfaFailureEvent(authentication: Authentication, exception: AuthenticationException) :
    AbstractAuthenticationFailureEvent(authentication, exception) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class MfaSuccessEvent(authentication: Authentication) : AbstractAuthenticationEvent(authentication) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@JvmInline
value class MfaSecret(private val content: String) {
    fun content() = content
}

@JvmInline
value class MfaChallenge(private val content: String) {
    fun content() = content
}

enum class MfaMethod { EMAIL_MFA_METHOD, SMS_MFA_METHOD}
data class MfaAccountMethod(
    val userName: String,
    val mfaDeviceId: MfaDeviceId,
    val key: Kid,
    val mfaMethod: MfaMethod,
    val mfaChannel: String,
    val associated: Boolean
)

class MfaException(message: String) : AuthenticationException(message) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class UnAssociatedMfaVerificationException(message: String) : AuthenticationException(message)
class AssociatedMfaVerificationException(message: String) : AuthenticationException(message)
