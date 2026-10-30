package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.TicketFixture
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import io.mockk.core.ValueClassSupport.maybeUnboxValueForMethodReturn
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

private const val RAW_TICKET = "A_TICKET"
private const val CODE = "CODE"


@ExtendWith(MockKExtension::class)
class MfaMethodsEnrollmentAssociationTest {
    private val account = AccountTestFixture.anAccount()
    private val email = account.email
    private val userName = email

    private val mfaAccountMethod = MfaAccountMethod(
        email,
        MfaDeviceId("AN_MFA_DEVICE_ID"),
        Kid("A_KID"),
        MfaMethod.EMAIL_MFA_METHOD,
        email,
        true
    )
    private val notAssociatedMfaAccountMethod = MfaAccountMethod(
        email,
        MfaDeviceId("AN_MFA_DEVICE_ID"),
        Kid("A_KID"),
        MfaMethod.EMAIL_MFA_METHOD,
        email,
        false
    )
    private val ticket = TicketFixture.ticketFor(
        RAW_TICKET,
        userName,
        ClientAppId.empty().content,
    )
    private val ticketId = TicketId(RAW_TICKET)

    @MockK
    lateinit var ticketRepository: TicketRepository

    @MockK
    lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    @MockK
    lateinit var mfaVerifier: MfaVerifier

    lateinit var underTest: MfaMethodsEnrollmentAssociation

    @BeforeEach
    fun setUp() {
        underTest = MfaMethodsEnrollmentAssociation(ticketRepository, mfaAccountMethodsRepository, mfaVerifier)
        every { ticketRepository.recordCodeAttempt(ticketId) } returns 1
    }

    @Test
    fun `when mfa is associated with auto association feature enabled`() {
        val ticketWithAutoAssociationFeatureEnabled =
            ticket.copy(
                context = TicketFixture.ticketContext(
                    userName,
                    Ticket.MFA_SELF_ASSOCIATION_CONTEXT_VALUE,
                    "AN_MFA_DEVICE_ID",
                    Ticket.EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
                )
            )
        every { ticketRepository.loadFor(ticketId) } returns ticketWithAutoAssociationFeatureEnabled
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod

        every {
            mfaAccountMethodsRepository.save(
                userName,
                ticket.context.mfaMethod(),
                ticket.context.mfaChannel(),
                true
            )
        } returns mfaAccountMethod
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns MfaDeviceId("ANOTHER_MFA_DEVICE_ID")
        every { ticketRepository.delete(ticketWithAutoAssociationFeatureEnabled.ticketId) } just runs

        underTest.associate(RAW_TICKET)

        verify { ticketRepository.loadFor(ticketId) }
        verify { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())}

        verify {
            mfaAccountMethodsRepository.save(
                userName,
                ticket.context.mfaMethod(),
                ticket.context.mfaChannel(),
                true
            )
        }
        verify { ticketRepository.delete(ticketWithAutoAssociationFeatureEnabled.ticketId) }
    }

    @Test
    fun `when mfa is associated`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod

        every {
            mfaVerifier.verifyMfaChallengeToBeAssociatedFor(
                userName,
                ticket.context.mfaDeviceId(),
                MfaChallenge(CODE)
            )
        } just runs
        every {
            mfaAccountMethodsRepository.save(
                userName,
                ticket.context.mfaMethod(),
                ticket.context.mfaChannel(),
                true
            )
        } returns mfaAccountMethod
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns MfaDeviceId("ANOTHER_MFA_DEVICE_ID")
        every { ticketRepository.delete(ticket.ticketId) } just runs


        underTest.associate(userName, RAW_TICKET, CODE)

        verify { ticketRepository.loadFor(ticketId) }
        verify { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())}

        verify {
            mfaAccountMethodsRepository.save(
                userName,
                ticket.context.mfaMethod(),
                ticket.context.mfaChannel(),
                true
            )
        }
        verify {
            mfaVerifier.verifyMfaChallengeToBeAssociatedFor(
                userName,
                ticket.context.mfaDeviceId(),
                MfaChallenge(CODE)
            )
        }
        verify { ticketRepository.delete(ticket.ticketId) }
    }

    @Test
    fun `when a ticket is expired`() {
        every { ticketRepository.loadFor(ticketId) } returns null

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify { ticketRepository.loadFor(ticketId) }
    }

    @Test
    fun `when a multiple ticket has been created after one association all the remaining ticket should be not valid anymore`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns mfaAccountMethod
        every { ticketRepository.delete(ticket.ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.ALREADY_ASSOCIATED) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify { ticketRepository.loadFor(ticketId) }
        verify { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())}
        verify { ticketRepository.delete(ticket.ticketId) }
    }

    @Test
    fun `when the mfa device of the ticket no longer exists the association is rejected`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns null
        every { ticketRepository.delete(ticket.ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
        verify { ticketRepository.delete(ticket.ticketId) }
    }

    @Test
    fun `when the mfa device of a self association ticket no longer exists the association is rejected`() {
        val selfAssociationTicket =
            ticket.copy(context = emailVerificationTicketContext())
        every { ticketRepository.loadFor(ticketId) } returns selfAssociationTicket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns null
        every { ticketRepository.delete(selfAssociationTicket.ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(RAW_TICKET) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when the channel of the ticket was enrolled again as a new mfa device the association is rejected`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns
                notAssociatedMfaAccountMethod.copy(mfaDeviceId = MfaDeviceId("A_NEW_MFA_DEVICE_ID"))
        every { ticketRepository.delete(ticket.ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when mfa is associated on an account without a default mfa device it becomes the default`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } just runs
        every { mfaAccountMethodsRepository.save(userName, ticket.context.mfaMethod(), ticket.context.mfaChannel(), true) } returns mfaAccountMethod
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns null
        every { mfaAccountMethodsRepository.setAsDefault(userName, mfaAccountMethod.mfaDeviceId) } just runs
        every { ticketRepository.delete(ticket.ticketId) } just runs

        underTest.associate(userName, RAW_TICKET, CODE)

        verify { mfaAccountMethodsRepository.setAsDefault(userName, mfaAccountMethod.mfaDeviceId) }
    }

    @Test
    fun `when mfa is associated on an account with a default mfa device the default is kept`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } just runs
        every { mfaAccountMethodsRepository.save(userName, ticket.context.mfaMethod(), ticket.context.mfaChannel(), true) } returns mfaAccountMethod
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns MfaDeviceId("ANOTHER_MFA_DEVICE_ID")
        every { ticketRepository.delete(ticket.ticketId) } just runs

        underTest.associate(userName, RAW_TICKET, CODE)

        verify(exactly = 0) { mfaAccountMethodsRepository.setAsDefault(any(), any()) }
    }

    @Test
    fun `when the code is wrong the association fails and the ticket can be used again`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } throws
                WrongMfaCodeException("Customer Code does not match with system code")
        every { ticketRepository.recordCodeAttempt(ticketId) } returns 4

        assertFailsWith(MfaAssociationFailureReason.WRONG_CODE) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
        verify(exactly = 0) { ticketRepository.delete(any()) }
    }

    @Test
    fun `when the ticket was issued to another account the association fails and the ticket is kept`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) {
            underTest.associate("another_$userName", RAW_TICKET, CODE)
        }

        verify(exactly = 0) { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(any(), any(), any()) }
        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
        verify(exactly = 0) { ticketRepository.delete(any()) }
    }

    @Test
    fun `when the ticket has no mfa enrollment purpose the association fails`() {
        every { ticketRepository.loadFor(ticketId) } returns
                ticket.copy(context = TicketFixture.ticketContext(userName, purpose = null))

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(any(), any(), any()) }
        verify(exactly = 0) { ticketRepository.delete(any()) }
    }

    @Test
    fun `when an email verification ticket is used to associate with a code the association fails`() {
        every { ticketRepository.loadFor(ticketId) } returns
                ticket.copy(
                    context = TicketFixture.ticketContext(
                        userName,
                        Ticket.MFA_SELF_ASSOCIATION_CONTEXT_VALUE,
                        purpose = Ticket.EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
                    )
                )

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when a reset password ticket is used to associate the association fails`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket.copy(context = TicketContext.resetPasswordContext())

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { ticketRepository.delete(any()) }
    }

    @Test
    fun `when the verifier finds the device already associated the association fails as already associated`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } throws
                AssociatedMfaVerificationException("already associated")

        assertFailsWith(MfaAssociationFailureReason.ALREADY_ASSOCIATED) { underTest.associate(userName, RAW_TICKET, CODE) }
    }

    @Test
    fun `when the verifier no longer finds the device the association fails as invalid ticket`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } throws
                MfaDeviceNotFoundException("not found")
        every { ticketRepository.delete(ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify { ticketRepository.delete(ticketId) }
    }

    @Test
    fun `when a self association ticket is for an already associated device the association fails as already associated`() {
        every { ticketRepository.loadFor(ticketId) } returns
                ticket.copy(context = emailVerificationTicketContext())
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns mfaAccountMethod
        every { ticketRepository.delete(ticket.ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.ALREADY_ASSOCIATED) { underTest.associate(RAW_TICKET) }
    }

    @Test
    fun `when a ticket without self association is redeemed without a code the association fails`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(RAW_TICKET) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when a self association ticket has no email verification purpose the association fails`() {
        every { ticketRepository.loadFor(ticketId) } returns
                ticket.copy(context = TicketFixture.ticketContext(userName, Ticket.MFA_SELF_ASSOCIATION_CONTEXT_VALUE, purpose = null))

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(RAW_TICKET) }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
        verify(exactly = 0) { ticketRepository.delete(any()) }
    }

    @Test
    fun `when the fifth wrong code is given the ticket is consumed and the association fails as invalid ticket`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } throws
                WrongMfaCodeException("Customer Code does not match with system code")
        every { ticketRepository.recordCodeAttempt(ticketId) } returns 5
        every { ticketRepository.delete(ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify { ticketRepository.delete(ticketId) }
        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when the ticket disappears before its code is checked the association fails as invalid ticket`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { ticketRepository.recordCodeAttempt(ticketId) } returns null

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(any(), any(), any()) }
        verify(exactly = 0) { ticketRepository.delete(any()) }
    }

    @Test
    fun `when the ticket already had five codes checked a further code is not checked`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { ticketRepository.recordCodeAttempt(ticketId) } returns 6
        every { ticketRepository.delete(ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify(exactly = 0) { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(any(), any(), any()) }
        verify { ticketRepository.delete(ticketId) }
    }

    @Test
    fun `when the fifth code is right the device is associated`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { ticketRepository.recordCodeAttempt(ticketId) } returns 5
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } just runs
        every { mfaAccountMethodsRepository.save(userName, ticket.context.mfaMethod(), ticket.context.mfaChannel(), true) } returns mfaAccountMethod
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns MfaDeviceId("ANOTHER_MFA_DEVICE_ID")
        every { ticketRepository.delete(ticketId) } just runs

        underTest.associate(userName, RAW_TICKET, CODE)

        verify { mfaAccountMethodsRepository.save(userName, ticket.context.mfaMethod(), ticket.context.mfaChannel(), true) }
    }

    @Test
    fun `when the verification fails for a reason other than the code the association fails as invalid ticket`() {
        every { ticketRepository.loadFor(ticketId) } returns ticket
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns notAssociatedMfaAccountMethod
        every { mfaVerifier.verifyMfaChallengeToBeAssociatedFor(userName, ticket.context.mfaDeviceId(), MfaChallenge(CODE)) } throws
                MfaException("No secret key found")
        every { ticketRepository.delete(ticketId) } just runs

        assertFailsWith(MfaAssociationFailureReason.INVALID_TICKET) { underTest.associate(userName, RAW_TICKET, CODE) }

        verify { ticketRepository.delete(ticketId) }
    }

    private fun emailVerificationTicketContext() = TicketFixture.ticketContext(
        userName,
        Ticket.MFA_SELF_ASSOCIATION_CONTEXT_VALUE,
        purpose = Ticket.EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
    )

    private fun assertFailsWith(reason: MfaAssociationFailureReason, association: () -> Unit) {
        val failure = Assertions.assertThrows(MfaAssociationFailureException::class.java) { association() }
        assertEquals(reason, failure.reason)
    }
}
