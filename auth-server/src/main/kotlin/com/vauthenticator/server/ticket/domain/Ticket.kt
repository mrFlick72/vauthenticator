package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_CHANNEL_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_DEVICE_ID_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_ENROLLMENT_PURPOSE_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_METHOD_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_NOT_SELF_ASSOCIATION_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_SELF_ASSOCIATION_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_SELF_ASSOCIATION_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.Ticket.Companion.PURPOSE_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.RESET_PASSWORD_PURPOSE_CONTEXT_VALUE
import java.time.Duration

data class Ticket(
    val ticketId: TicketId,
    val userName: String,
    val clientAppId: String,
    val ttl: Long,
    val context: TicketContext = TicketContext.empty(),
) {
    companion object {
        const val MFA_DEVICE_ID_CONTEXT_KEY = "mfaDeviceId"
        const val MFA_CHANNEL_CONTEXT_KEY = "mfaChannel"
        const val MFA_METHOD_CONTEXT_KEY = "mfaMethod"
        const val MFA_SELF_ASSOCIATION_CONTEXT_KEY = "selfAssociation"
        const val MFA_SELF_ASSOCIATION_CONTEXT_VALUE = "true"
        const val MFA_NOT_SELF_ASSOCIATION_CONTEXT_VALUE = "false"
        const val PURPOSE_CONTEXT_KEY = "purpose"
        const val RESET_PASSWORD_PURPOSE_CONTEXT_VALUE = "resetPassword"
        const val MFA_ENROLLMENT_PURPOSE_CONTEXT_VALUE = "mfaEnrollment"
        const val EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE = "emailVerification"
    }
}

data class TicketContext(val content: Map<String, String>) {

    companion object {
        fun empty() = TicketContext(emptyMap())
        fun resetPasswordContext() = TicketContext(mapOf(PURPOSE_CONTEXT_KEY to RESET_PASSWORD_PURPOSE_CONTEXT_VALUE))
        fun mfaContextFor(
            mfaMethod: MfaMethod,
            mfaChannel: String,
            mfaDeviceId: String,
            autoAssociation: Boolean = false,
            purpose: String = MFA_ENROLLMENT_PURPOSE_CONTEXT_VALUE
        ) = TicketContext(
            mapOf(
                MFA_DEVICE_ID_CONTEXT_KEY to mfaDeviceId,
                MFA_CHANNEL_CONTEXT_KEY to mfaChannel,
                MFA_METHOD_CONTEXT_KEY to mfaMethod.name,
                MFA_SELF_ASSOCIATION_CONTEXT_KEY to if (autoAssociation) {
                    MFA_SELF_ASSOCIATION_CONTEXT_VALUE
                } else {
                    MFA_NOT_SELF_ASSOCIATION_CONTEXT_VALUE
                },
                PURPOSE_CONTEXT_KEY to purpose
            )
        )
    }

    fun isForResetPassword() = content[PURPOSE_CONTEXT_KEY] == RESET_PASSWORD_PURPOSE_CONTEXT_VALUE
    fun isForMfaEnrollment() = content[PURPOSE_CONTEXT_KEY] == MFA_ENROLLMENT_PURPOSE_CONTEXT_VALUE
    fun isMfaNotSelfAssociable() = content[MFA_SELF_ASSOCIATION_CONTEXT_KEY] != MFA_SELF_ASSOCIATION_CONTEXT_VALUE
    fun mfaMethod(): MfaMethod {
        val mfaMethod = requiredValue(MFA_METHOD_CONTEXT_KEY)
        return try {
            MfaMethod.valueOf(mfaMethod)
        } catch (_: IllegalArgumentException) {
            throw InvalidTicketException("The ticket context is not valid, invalid $MFA_METHOD_CONTEXT_KEY value: $mfaMethod")
        }
    }

    fun mfaChannel() = requiredValue(MFA_CHANNEL_CONTEXT_KEY)
    fun mfaDeviceId() = MfaDeviceId(requiredValue(MFA_DEVICE_ID_CONTEXT_KEY))

    private fun requiredValue(key: String) =
        content[key] ?: throw InvalidTicketException("The ticket context is not valid, missing $key")
}

data class TicketId(val content: String)

data class TicketFeatures(val ttl: Duration)

class InvalidTicketException(message: String) : RuntimeException(message)
