package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.mfa.domain.MfaAssociationFailureReason.*
import com.vauthenticator.server.ticket.domain.*

typealias MfaAssociationVerifier = (ticket: Ticket) -> Unit

const val MAX_ASSOCIATION_CODE_ATTEMPTS = 5

class MfaMethodsEnrollmentAssociation(
    private val ticketRepository: TicketRepository,
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    private val mfaVerifier: MfaVerifier
) {

    fun associate(ticketId: String, asDefaultMethod: Boolean = false) {
        associate(
            ticketId,
            asDefaultMethod,
            redeemable = { it.context.isForEmailVerification() }
        ) {
            if (it.context.isMfaNotSelfAssociable()) {
                throw MfaAssociationFailureException(
                    INVALID_TICKET,
                    "Mfa association without code is allowed only if in the ticket context there is ${Ticket.MFA_SELF_ASSOCIATION_CONTEXT_KEY} feature enabled"
                )
            }
        }

    }

    fun associate(userName: String, ticketId: String, code: String, asDefaultMethod: Boolean = false) {
        associate(
            ticketId,
            asDefaultMethod,
            // a ticket of another account or flow is left in place: it is not this caller's to consume
            redeemable = { it.userName == userName && it.context.isForMfaEnrollment() }
        ) {
            // counted before the check, so parallel requests cannot get more codes checked than the limit
            val codeAttempts = ticketRepository.recordCodeAttempt(it.ticketId)
            if (codeAttempts == null || codeAttempts > MAX_ASSOCIATION_CODE_ATTEMPTS) {
                throw tooManyCodesFailureFor(it)
            }
            try {
                mfaVerifier.verifyMfaChallengeToBeAssociatedFor(
                    it.userName,
                    it.context.mfaDeviceId(),
                    MfaChallenge(code)
                )
            } catch (e: MfaException) {
                // the wrong code that reaches the limit consumes the ticket: WRONG_CODE promises a ticket that can be retried
                throw if (codeAttempts == MAX_ASSOCIATION_CODE_ATTEMPTS) tooManyCodesFailureFor(it)
                else MfaAssociationFailureException(WRONG_CODE, e.message.orEmpty())
            } catch (e: MfaDeviceNotFoundException) {
                throw MfaAssociationFailureException(INVALID_TICKET, e.message.orEmpty())
            } catch (e: AssociatedMfaVerificationException) {
                throw MfaAssociationFailureException(ALREADY_ASSOCIATED, e.message.orEmpty())
            }
        }
    }

    private fun associate(
        ticket: String,
        asDefaultMethod: Boolean,
        redeemable: (Ticket) -> Boolean,
        verifier: MfaAssociationVerifier
    ) {
        val loadedTicket = ticketRepository.loadFor(TicketId(ticket))
            ?.takeIf(redeemable)
            ?: throw MfaAssociationFailureException(INVALID_TICKET, "The ticket $ticket is not a valid ticket, it seems to be expired")
        val (mfaMethod, mfaChannel, mfaDeviceId) = try {
            Triple(loadedTicket.context.mfaMethod(), loadedTicket.context.mfaChannel(), loadedTicket.context.mfaDeviceId())
        } catch (e: InvalidTicketException) {
            throw MfaAssociationFailureException(INVALID_TICKET, e.message.orEmpty())
        }
        val enrolledMfaAccountMethod = mfaAccountMethodsRepository.findBy(loadedTicket.userName, mfaMethod, mfaChannel)
            ?.takeIf { it.mfaDeviceId == mfaDeviceId }
            ?: run {
                revoke(loadedTicket)
                throw MfaAssociationFailureException(INVALID_TICKET, "The ticket $ticket is not a valid ticket, its mfa device no longer exists")
            }
        if (enrolledMfaAccountMethod.associated) {
            revoke(loadedTicket)
            throw MfaAssociationFailureException(ALREADY_ASSOCIATED, "The ticket $ticket is not a valid ticket, it seems that the mfa associated is already associated")
        }
        verifier.invoke(loadedTicket)
        val mfaAccountMethod = mfaAccountMethodsRepository.save(
            loadedTicket.userName,
            mfaMethod,
            mfaChannel,
            true
        )
        // an account with an associated device always has a default one: login challenges go to it
        if (asDefaultMethod || mfaAccountMethodsRepository.getDefaultDevice(loadedTicket.userName) == null) {
            mfaAccountMethodsRepository.setAsDefault(loadedTicket.userName, mfaAccountMethod.mfaDeviceId)
        }
        revoke(loadedTicket)
    }

    private fun tooManyCodesFailureFor(ticket: Ticket): MfaAssociationFailureException {
        revoke(ticket)
        return MfaAssociationFailureException(INVALID_TICKET, "The ticket ${ticket.ticketId.content} is not a valid ticket, too many wrong codes")
    }

    private fun revoke(ticket: Ticket) =
        ticketRepository.delete(ticket.ticketId)
}
