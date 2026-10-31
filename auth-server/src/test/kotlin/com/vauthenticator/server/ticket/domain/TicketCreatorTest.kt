package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.extentions.expirationTimeStampInSecondFromNow
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import com.vauthenticator.server.mfa.domain.MfaMethod.EMAIL_MFA_METHOD
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

private const val TICKET_ID = "A_TICKET_ID"

@ExtendWith(MockKExtension::class)
class TicketCreatorTest {
    private val ticketGenerator = { TICKET_ID }
    private val now = Instant.now()
    private val clock: Clock = Clock.fixed(now, ZoneId.systemDefault())

    @MockK
    private lateinit var ticketRepository: TicketRepository

    private val ticketFeatures: TicketFeatures = TicketFeatures(
        mfaEnrollment = Duration.ofMinutes(5),
        resetPassword = Duration.ofMinutes(15),
        emailVerification = Duration.ofHours(24)
    )
    private val clientAppId = ClientAppFixture.aClientAppId()
    private val account = AccountTestFixture.anAccount()
    private val mfaEnrollmentContext = TicketContext.mfaContextFor(EMAIL_MFA_METHOD, account.email, "A_DEVICE_ID")
    private val emailVerificationContext = TicketContext.mfaContextFor(
        EMAIL_MFA_METHOD, account.email, "A_DEVICE_ID", true, Ticket.EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
    )

    @Test
    fun `happy path`() {
        val uut = TicketCreator(ticketGenerator, clock, ticketRepository, ticketFeatures)
        every { ticketRepository.store(ticketExpiringIn(Duration.ofMinutes(5), mfaEnrollmentContext)) } just runs

        val createTicketFor = uut.createTicketFor(account, clientAppId, mfaEnrollmentContext)

        Assertions.assertEquals(TICKET_ID, createTicketFor.content)
    }

    @Test
    fun `a reset password ticket lives for the reset password ttl`() {
        val uut = TicketCreator(ticketGenerator, clock, ticketRepository, ticketFeatures)
        val context = TicketContext.resetPasswordContext()
        every { ticketRepository.store(any()) } just runs

        uut.createTicketFor(account, clientAppId, context)

        verify { ticketRepository.store(ticketExpiringIn(Duration.ofMinutes(15), context)) }
    }

    @Test
    fun `an email verification ticket lives for the email verification ttl`() {
        val uut = TicketCreator(ticketGenerator, clock, ticketRepository, ticketFeatures)
        every { ticketRepository.store(any()) } just runs

        uut.createTicketFor(account, clientAppId, emailVerificationContext)

        verify { ticketRepository.store(ticketExpiringIn(Duration.ofHours(24), emailVerificationContext)) }
    }

    @Test
    fun `a ticket without a known purpose lives for the shortest ttl`() {
        val uut = TicketCreator(ticketGenerator, clock, ticketRepository, ticketFeatures)
        every { ticketRepository.store(any()) } just runs

        uut.createTicketFor(account, clientAppId, TicketContext.empty())

        verify { ticketRepository.store(ticketExpiringIn(Duration.ofMinutes(5), TicketContext.empty())) }
    }

    private fun ticketExpiringIn(ttl: Duration, context: TicketContext) = Ticket(
        TicketId(TICKET_ID),
        account.email,
        clientAppId.content,
        ttl.expirationTimeStampInSecondFromNow(clock),
        context
    )
}
