package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.ticket.domain.*

typealias MfaAssociationVerifier = (ticket: Ticket) -> Unit

class MfaMethodsEnrollmentAssociation(
    private val ticketRepository: TicketRepository,
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    private val mfaVerifier: MfaVerifier
) {

    fun associate(ticketId: String, asDefaultMethod: Boolean = false) {
        associate(
            ticketId,
            asDefaultMethod
        ) {
            if (it.context.isMfaNotSelfAssociable()) {
                throw InvalidTicketException("Mfa association without code is allowed only if in the ticket context there is ${Ticket.MFA_SELF_ASSOCIATION_CONTEXT_KEY} feature enabled")
            }
        }

    }

    fun associate(ticketId: String, code: String, asDefaultMethod: Boolean = false) {
        associate(
            ticketId,
            asDefaultMethod
        ) {
            mfaVerifier.verifyMfaChallengeToBeAssociatedFor(
                it.userName,
                it.context.mfaDeviceId(),
                MfaChallenge(code)
            )
        }
    }

    private fun associate(ticket: String, asDefaultMethod: Boolean, verifier: MfaAssociationVerifier) {
        val loadedTicket = ticketRepository.loadFor(TicketId(ticket))
            ?: throw InvalidTicketException("The ticket $ticket is not a valid ticket, it seems to be expired")
        val enrolledMfaAccountMethod = mfaAccountMethodsRepository.findBy(loadedTicket.userName, loadedTicket.context.mfaMethod(), loadedTicket.context.mfaChannel())
            ?.takeIf { it.mfaDeviceId == loadedTicket.context.mfaDeviceId() }
            ?: run {
                revoke(loadedTicket)
                throw InvalidTicketException("The ticket $ticket is not a valid ticket, its mfa device no longer exists")
            }
        if (enrolledMfaAccountMethod.associated) {
            revoke(loadedTicket)
            throw InvalidTicketException("The ticket $ticket is not a valid ticket, it seems that the mfa associated is already associated", InvalidTicketCause.ALREADY_ASSOCIATED_MFA)
        }
        verifier.invoke(loadedTicket)
        val mfaAccountMethod = mfaAccountMethodsRepository.save(
            loadedTicket.userName,
            loadedTicket.context.mfaMethod(),
            loadedTicket.context.mfaChannel(),
            true
        )
        // an account with an associated device always has a default one: login challenges go to it
        if (asDefaultMethod || mfaAccountMethodsRepository.getDefaultDevice(loadedTicket.userName) == null) {
            mfaAccountMethodsRepository.setAsDefault(loadedTicket.userName, mfaAccountMethod.mfaDeviceId)
        }
        revoke(loadedTicket)
    }

    private fun revoke(ticket: Ticket) =
        ticketRepository.delete(ticket.ticketId)
}
