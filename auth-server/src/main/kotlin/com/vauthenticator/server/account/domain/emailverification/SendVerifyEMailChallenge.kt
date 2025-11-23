package com.vauthenticator.server.account.domain.emailverification

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollment
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_SELF_ASSOCIATION_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.TicketId
import org.slf4j.LoggerFactory

private const val LINK_KEY = "verificationEMailLink"

class SendVerifyEMailChallenge(
    private val accountRepository: AccountRepository,
    private val mfaMethodsEnrollment: MfaMethodsEnrollment,
    private val mailVerificationMailSender: EMailSenderService,
    private val frontChannelBaseUrl: String
) {

    private val logger = LoggerFactory.getLogger(SendVerifyEMailChallenge::class.java)

    fun sendVerifyMail(email: String): Unit = accountRepository.accountFor(email)
        .map { account ->
            val verificationTicket =
                mfaMethodsEnrollment.enroll(
                    account.email,
                    MfaMethod.EMAIL_MFA_METHOD,
                    account.email,
                    ClientAppId.empty(),
                    false,
                    mapOf(Ticket.MFA_SELF_ASSOCIATION_CONTEXT_KEY to MFA_SELF_ASSOCIATION_CONTEXT_VALUE)
                )
            val mailContext = mailContextFrom(verificationTicket)
            mailVerificationMailSender.sendFor(account, mailContext)
        }.orElseThrow {
            logger.warn("account not found")
            AccountNotFoundException("account not found")
        }

    private fun mailContextFrom(ticketId: TicketId): Map<String, String> {
        val verificationLink = "$frontChannelBaseUrl/email-verify/${ticketId.content}"
        return mapOf(LINK_KEY to verificationLink)
    }

}