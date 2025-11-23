package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.TicketFixture
import com.vauthenticator.server.ticket.domain.InvalidTicketException
import com.vauthenticator.server.ticket.domain.Ticket
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
        MfaDeviceId("A_MFA_DEVICE_ID"),
        Kid("A_KID"),
        MfaMethod.EMAIL_MFA_METHOD,
        email,
        true
    )
    private val notAssociatedMfaAccountMethod = MfaAccountMethod(
        email,
        MfaDeviceId("A_MFA_DEVICE_ID"),
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
    }

    @Test
    fun `when mfa is associated with auto association feature enabled`() {
        val ticketWithAutoAssociationFeatureEnabled =
            ticket.copy(
                context = TicketFixture.ticketContext(
                    userName,
                    Ticket.MFA_SELF_ASSOCIATION_CONTEXT_VALUE,
                    "A_MFA_DEVICE_ID"
                )
            )
        every { ticketRepository.loadFor(ticketId) } returns Optional.of(ticketWithAutoAssociationFeatureEnabled)
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns Optional.of(notAssociatedMfaAccountMethod)

        every {
            mfaAccountMethodsRepository.save(
                userName,
                ticket.context.mfaMethod(),
                ticket.context.mfaChannel(),
                true
            )
        } returns mfaAccountMethod
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
        every { ticketRepository.loadFor(ticketId) } returns Optional.of(ticket)
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns Optional.of(notAssociatedMfaAccountMethod)

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
        every { ticketRepository.delete(ticket.ticketId) } just runs


        underTest.associate(RAW_TICKET, CODE)

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
        every { ticketRepository.loadFor(ticketId) } returns Optional.empty()

        Assertions.assertThrows(InvalidTicketException::class.java) { underTest.associate(RAW_TICKET, CODE) }

        verify { ticketRepository.loadFor(ticketId) }
    }

    @Test
    fun `when a multiple ticket has been created after one association all the remaining ticket should be not valid anymore`() {
        every { ticketRepository.loadFor(ticketId) } returns Optional.of(ticket)
        every { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())} returns Optional.of(mfaAccountMethod)
        every { ticketRepository.delete(ticket.ticketId) } just runs

        Assertions.assertThrows(InvalidTicketException::class.java) { underTest.associate(RAW_TICKET, CODE) }

        verify { ticketRepository.loadFor(ticketId) }
        verify { mfaAccountMethodsRepository.findBy(ticket.userName, ticket.context.mfaMethod(), ticket.context.mfaChannel())}
        verify { ticketRepository.delete(ticket.ticketId) }
    }
}