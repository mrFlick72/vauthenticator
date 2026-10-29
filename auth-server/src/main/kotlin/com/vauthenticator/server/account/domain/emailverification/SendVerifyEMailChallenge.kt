package com.vauthenticator.server.account.domain.emailverification

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollment
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
        ?.let { account ->
            val verificationTicket = mfaMethodsEnrollment.enrollForEmailVerification(account.email)
            val mailContext = mailContextFrom(verificationTicket)
            mailVerificationMailSender.sendFor(account, mailContext)
        } ?: throw run {
            logger.warn("account not found")
            AccountNotFoundException("account not found")
        }

    private fun mailContextFrom(ticketId: TicketId): Map<String, String> {
        val verificationLink = "$frontChannelBaseUrl/email-verify/${ticketId.content}"
        return mapOf(LINK_KEY to verificationLink)
    }

}
