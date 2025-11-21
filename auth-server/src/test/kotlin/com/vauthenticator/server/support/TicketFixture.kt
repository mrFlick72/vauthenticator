package com.vauthenticator.server.support

import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketId

object TicketFixture {

    fun ticketContext(email: String, selfAssociation: String = "false", mfaDeviceId: String = "AN_MFA_DEVICE_ID") = TicketContext(
        mapOf(
            "mfaDeviceId" to mfaDeviceId,
            "mfaChannel" to email,
            "mfaMethod" to MfaMethod.EMAIL_MFA_METHOD.name,
            "selfAssociation" to selfAssociation
        )
    )

    fun ticketFor(verificationTicketValue: String, email: String, clientAppId: String): Ticket {

        return Ticket(
            TicketId(verificationTicketValue), email, clientAppId, 200,
            ticketContext(email)
        )
    }
}