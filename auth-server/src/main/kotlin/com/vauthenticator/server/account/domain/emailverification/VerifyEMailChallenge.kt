package com.vauthenticator.server.account.domain.emailverification

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollmentAssociation
import com.vauthenticator.server.ticket.domain.InvalidTicketCause
import com.vauthenticator.server.ticket.domain.InvalidTicketException
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class VerifyEMailChallenge(
    private val ticketRepository: TicketRepository,
    private val accountRepository: AccountRepository,
    private val mfaMethodsEnrollmentAssociation: MfaMethodsEnrollmentAssociation
) {

    private val logger: Logger = LoggerFactory.getLogger(VerifyEMailChallenge::class.java)

    fun verifyMail(ticket: String) {
        ticketRepository.loadFor(TicketId(ticket))
            .map {
                try {
                    mfaMethodsEnrollmentAssociation.associate(ticket, true)
                } catch (e: InvalidTicketException) {
                    logger.debug(e.message, e)
                    logger.debug("Reason: ${e.reason.name}")
                    if (e.reason != InvalidTicketCause.ALREADY_ASSOCIATED_MFA) {
                        throw e
                    }
                }
                enableAccountFrom(it.userName)
            }
            .orElseThrow { throw InvalidTicketException("The ticket $ticket is not a valid ticket") }

    }

    private fun enableAccountFrom(email: String): Account =
        accountRepository.accountFor(email)
            .map { account ->
                val enabledAccount = makeAnAccountEnableForm(account)
                accountRepository.save(enabledAccount)
                enabledAccount
            }
            .orElseThrow { throw InvalidTicketException("The ticket associated with the username $email is not a valid ticket") }


    private fun makeAnAccountEnableForm(account: Account) =
        account.copy(accountNonLocked = true, enabled = true, emailVerified = true)

}

