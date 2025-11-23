package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_CHANNEL_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_DEVICE_ID_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_METHOD_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_NOT_SELF_ASSOCIATION_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_SELF_ASSOCIATION_CONTEXT_KEY
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_SELF_ASSOCIATION_CONTEXT_VALUE
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
    }
}

data class TicketContext(val content: Map<String, String>) {

    companion object {
        fun empty() = TicketContext(emptyMap())
        fun mfaContextFor(
            mfaMethod: MfaMethod,
            mfaChannel: String,
            mfaDeviceId: String,
            autoAssociation: Boolean = false,
            ticketContextAdditionalProperties: Map<String, String>
        ) = TicketContext(
            mapOf(
                MFA_DEVICE_ID_CONTEXT_KEY to mfaDeviceId,
                MFA_CHANNEL_CONTEXT_KEY to mfaChannel,
                MFA_METHOD_CONTEXT_KEY to mfaMethod.name,
                MFA_SELF_ASSOCIATION_CONTEXT_KEY to if (autoAssociation) {
                    MFA_SELF_ASSOCIATION_CONTEXT_VALUE
                } else {
                    MFA_NOT_SELF_ASSOCIATION_CONTEXT_VALUE
                }
            ) + ticketContextAdditionalProperties
        )
    }

    fun isMfaNotSelfAssociable() = content[MFA_SELF_ASSOCIATION_CONTEXT_KEY] != MFA_SELF_ASSOCIATION_CONTEXT_VALUE
    fun mfaMethod() = MfaMethod.valueOf(content[MFA_METHOD_CONTEXT_KEY]!!)
    fun mfaChannel() = content[MFA_CHANNEL_CONTEXT_KEY]!!
    fun mfaDeviceId() = MfaDeviceId(content[MFA_DEVICE_ID_CONTEXT_KEY]!!)
}

data class TicketId(val content: String)

data class TicketFeatures(val ttl: Duration)

enum class InvalidTicketCause {
    TICKET_EXPIRED,
    ALREADY_ASSOCIATED_MFA
}
class InvalidTicketException(message: String, val reason : InvalidTicketCause = InvalidTicketCause.TICKET_EXPIRED) : RuntimeException(message)