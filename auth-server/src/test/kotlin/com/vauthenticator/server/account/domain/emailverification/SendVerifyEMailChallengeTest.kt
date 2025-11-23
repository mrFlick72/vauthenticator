package com.vauthenticator.server.account.domain.emailverification

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallenge
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollment
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.Ticket.Companion.MFA_SELF_ASSOCIATION_CONTEXT_VALUE
import com.vauthenticator.server.ticket.domain.TicketId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class SendVerifyEMailChallengeTest {

    @MockK
    lateinit var clientAccountRepository: ClientApplicationRepository

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var mfaMethodsEnrollment: MfaMethodsEnrollment

    @MockK
    lateinit var mailVerificationMailSender: EMailSenderService

    private lateinit var underTest: SendVerifyEMailChallenge

    @BeforeEach
    fun setup() {
        underTest = SendVerifyEMailChallenge(
            accountRepository,
            mfaMethodsEnrollment,
            mailVerificationMailSender,
            "https://vauthenticator.com"
        )
    }

    @Test
    internal fun `happy path`() {
        val account = anAccount()
        val ticketId = TicketId("A_TICKET")
        val requestContext = mapOf("verificationEMailLink" to "https://vauthenticator.com/email-verify/A_TICKET")


        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every {
            mfaMethodsEnrollment.enroll(
                account.email,
                MfaMethod.EMAIL_MFA_METHOD,
                account.email,
                ClientAppId.empty(),
                false,
                mapOf(Ticket.MFA_SELF_ASSOCIATION_CONTEXT_KEY to MFA_SELF_ASSOCIATION_CONTEXT_VALUE)
            )
        } returns ticketId
        every { mailVerificationMailSender.sendFor(account, requestContext) } just runs

        underTest.sendVerifyMail(account.email)

        verify { mailVerificationMailSender.sendFor(account, requestContext) }
    }

    @Test
    internal fun `when account does not exist`() {
        val email = "anemail@test.com"
        val clientAppId = ClientAppId(A_CLIENT_APP_ID)
        val clientApplication = aClientApp(clientAppId).copy(scopes = Scopes.from(Scope.MAIL_VERIFY))

        every { clientAccountRepository.findOne(clientAppId) } returns Optional.of(clientApplication)
        every { accountRepository.accountFor(email) } returns Optional.empty()

        Assertions.assertThrows(AccountNotFoundException::class.java) {
            underTest.sendVerifyMail(email)
        }
    }

}