package com.vauthenticator.server.account.domain.emailverification

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.emailverification.VerifyEMailChallenge
import com.vauthenticator.server.mfa.domain.MfaMethodsEnrollmentAssociation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.TicketFixture
import com.vauthenticator.server.ticket.domain.InvalidTicketException
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

private const val RAW_TICKET = "A_TICKET"

@ExtendWith(MockKExtension::class)
internal class VerifyEMailChallengeTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var ticketRepository: TicketRepository

    @MockK
    lateinit var mfaMethodsEnrollmentAssociation: MfaMethodsEnrollmentAssociation


    private lateinit var underTest: VerifyEMailChallenge

    @BeforeEach
    fun setup() {
        underTest = VerifyEMailChallenge(
            ticketRepository,
            accountRepository,
            mfaMethodsEnrollmentAssociation
        )
    }

    @Test
    internal fun `happy path`() {
        val account = AccountTestFixture.anAccount()
        val enabledAccount = account.copy(accountNonLocked = true, enabled = true, emailVerified = true)
        val ticketId = TicketId(RAW_TICKET)

        every { ticketRepository.loadFor(ticketId) } returns Optional.of(
            TicketFixture.ticketFor(
                ticketId.content,
                account.email,
                ClientAppId.empty().content
            )
        )
        every { mfaMethodsEnrollmentAssociation.associate(RAW_TICKET, true) } just runs
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every { accountRepository.save(enabledAccount) } just runs

        underTest.verifyMail(RAW_TICKET)
        verify(exactly = 1) { mfaMethodsEnrollmentAssociation.associate(RAW_TICKET, true) }
    }

    @Test
    internal fun `when the account does not exist`() {
        val account = AccountTestFixture.anAccount()
        val ticketId = TicketId(RAW_TICKET)

        every { ticketRepository.loadFor(ticketId) } returns Optional.of(
            TicketFixture.ticketFor(
                ticketId.content,
                account.email,
                ClientAppId.empty().content
            )
        )
        every { accountRepository.accountFor(account.email) } returns Optional.empty()
        every { mfaMethodsEnrollmentAssociation.associate(RAW_TICKET, true) } just runs

        assertThrows(InvalidTicketException::class.java) { underTest.verifyMail(RAW_TICKET) }
    }

    @Test
    internal fun `when the ticket does not exist`() {
        val ticketId = TicketId(RAW_TICKET)

        every { ticketRepository.loadFor(ticketId) } returns Optional.empty()

        assertThrows(InvalidTicketException::class.java) { underTest.verifyMail(RAW_TICKET) }
    }

}