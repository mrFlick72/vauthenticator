package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.ticket.domain.*
import java.util.*

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
        ticketRepository.loadFor(TicketId(ticket))
            .map { ticket ->
                mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())
                    .ifPresent{
                        if(it.associated){
                            revoke(ticket)
                            throw InvalidTicketException("The ticket $ticket is not a valid ticket, it seems that the mfa associated is already associated",InvalidTicketCause.ALREADY_ASSOCIATED_MFA)
                        }
                    }
                verifier.invoke(ticket)
                val mfaAccountMethod = mfaAccountMethodsRepository.save(
                    ticket.userName,
                    ticket.context.mfaMethod(),
                    ticket.context.mfaChannel(),
                    true
                )
                if (asDefaultMethod) {
                    mfaAccountMethodsRepository.setAsDefault(ticket.userName, mfaAccountMethod.mfaDeviceId)
                }
                revoke(ticket)
            }
            .orElseThrow { throw InvalidTicketException("The ticket $ticket is not a valid ticket, it seems to be expired") }
    }

    private fun revoke(ticket: Ticket) =
        ticketRepository.delete(ticket.ticketId)
}