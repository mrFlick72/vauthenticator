package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mask.SensitiveDataMaskerResolver
import com.vauthenticator.server.mask.SensitiveEmailMasker
import com.vauthenticator.server.mfa.domain.MfaMethod.EMAIL_MFA_METHOD
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.MfaFixture.email
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.MfaFixture.associatedMfaAccountMethod
import com.vauthenticator.server.support.MfaFixture.notAssociatedMfaAccountMethod
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.support.TicketFixture.ticketContext
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketRepository
import com.vauthenticator.server.ticket.domain.TicketId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class MfaMethodsEnrollmentTest {

    @MockK
    private lateinit var sensitiveEmailMasker: SensitiveEmailMasker

    @MockK
    private lateinit var ticketCreator: TicketCreator

    @MockK
    private lateinit var ticketRepository: TicketRepository

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var mfaSender: MfaChallengeSender

    @MockK
    private lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    private lateinit var uut: MfaMethodsEnrollment

    private val account = anAccount()
    private val userName = account.email

    private val clientAppId = aClientAppId()
    private val ticketId = TicketId("A_TICKET")
    private val emailMfaAccountMethod = notAssociatedMfaAccountMethod(userName, email, EMAIL_MFA_METHOD)

    private val anotherMfaDeviceId = MfaDeviceId("A_NEW_MFA_DEVICE_ID")
    private val anotherEmail = "irrelevant_$email"
    private val anotherMfaDevice = emailMfaAccountMethod
        .copy(mfaDeviceId = anotherMfaDeviceId, mfaChannel = anotherEmail)

    private val associatedDefaultDevice = emailMfaAccountMethod.copy(associated = true)
    private val associatedOtherDevice = anotherMfaDevice.copy(associated = true)
    private val pendingMfaDeviceId = MfaDeviceId("A_PENDING_MFA_DEVICE_ID")
    private val pendingEmail = "pending_$email"
    private val pendingDevice = emailMfaAccountMethod.copy(mfaDeviceId = pendingMfaDeviceId, mfaChannel = pendingEmail)


    @BeforeEach
    fun setUp() {
        every { ticketRepository.deleteMfaEnrollmentTicketsFor(any(), any()) } just runs
        uut = MfaMethodsEnrollment(
            accountRepository,
            ticketCreator,
            ticketRepository,
            mfaSender,
            mfaAccountMethodsRepository,
            SensitiveDataMaskerResolver(
                mapOf(EMAIL_MFA_METHOD to sensitiveEmailMasker)
            )
        )
    }

    @Test
    fun `when the enrolment do not send the verification code together the verification ticket`() {
        every {
            mfaAccountMethodsRepository.findBy(
                userName,
                EMAIL_MFA_METHOD,
                email
            )
        } returns emailMfaAccountMethod
        every { accountRepository.accountFor(userName) } returns account
        every {
            ticketCreator.createTicketFor(
                account, clientAppId, ticketContext(
                    email,
                    mfaDeviceId = mfaDeviceId.content
                )
            )
        } returns ticketId

        val actual = uut.enroll(userName, EMAIL_MFA_METHOD, email, clientAppId, false)

        verify {
            mfaAccountMethodsRepository.findBy(
                userName,
                EMAIL_MFA_METHOD,
                email
            )
        }
        verify { accountRepository.accountFor(userName) }
        verify {
            ticketCreator.createTicketFor(
                account, clientAppId, ticketContext(
                    email,
                    mfaDeviceId = mfaDeviceId.content
                )
            )
        }

        assertEquals(ticketId, actual)
    }

    @Test
    fun `when mfa device was not enrolled before `() {
        every {
            mfaAccountMethodsRepository.findBy(
                userName,
                EMAIL_MFA_METHOD,
                email
            )
        } returns null
        every {
            mfaAccountMethodsRepository.save(
                userName,
                EMAIL_MFA_METHOD,
                email,
                false
            )
        } returns emailMfaAccountMethod

        every { accountRepository.accountFor(userName) } returns account
        every {
            ticketCreator.createTicketFor(
                account, clientAppId, ticketContext(
                    email,
                    mfaDeviceId = mfaDeviceId.content
                )
            )
        } returns ticketId

        val actual = uut.enroll(userName, EMAIL_MFA_METHOD, email, clientAppId, false)

        verify {
            mfaAccountMethodsRepository.findBy(
                userName,
                EMAIL_MFA_METHOD,
                email
            )
        }
        verify {
            mfaAccountMethodsRepository.save(
                userName,
                EMAIL_MFA_METHOD,
                email,
                false
            )
        }
        verify { accountRepository.accountFor(userName) }
        verify {
            ticketCreator.createTicketFor(
                account, clientAppId, ticketContext(
                    email,
                    mfaDeviceId = mfaDeviceId.content
                )
            )
        }

        assertEquals(ticketId, actual)
    }

    @Test
    fun `when the enrolment send the verification code together the verification ticket`() {
        every {
            mfaAccountMethodsRepository.findBy(
                userName,
                EMAIL_MFA_METHOD,
                email
            )
        } returns emailMfaAccountMethod
        every { accountRepository.accountFor(userName) } returns account
        every {
            ticketCreator.createTicketFor(
                account, clientAppId, ticketContext(
                    email,
                    mfaDeviceId = mfaDeviceId.content
                )
            )
        } returns ticketId
        every {
            mfaSender.sendMfaChallengeFor(
                userName,
                mfaDeviceId
            )
        } just runs

        val actual = uut.enroll(userName, EMAIL_MFA_METHOD, email, clientAppId, true)

        verify {
            mfaAccountMethodsRepository.findBy(
                userName,
                EMAIL_MFA_METHOD,
                email
            )
        }
        verify { accountRepository.accountFor(userName) }
        verify {
            ticketCreator.createTicketFor(
                account, clientAppId, ticketContext(
                    email,
                    mfaDeviceId = mfaDeviceId.content
                )
            )
        }
        verify {
            mfaSender.sendMfaChallengeFor(
                userName,
                mfaDeviceId
            )
        }

        assertEquals(ticketId, actual)
    }

    @Test
    fun `find all mfa enrollments for an account lists only its associated devices by default`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns mfaDeviceId
        every { mfaAccountMethodsRepository.findAll(userName) } returns listOf(
            associatedDefaultDevice,
            associatedOtherDevice,
            pendingDevice
        )

        val actual = uut.getEnrollmentsFor(userName)

        val expected = listOf(
            MfaDevice(userName, EMAIL_MFA_METHOD, email, mfaDeviceId, default = true, associated = true),
            MfaDevice(userName, EMAIL_MFA_METHOD, anotherEmail, anotherMfaDeviceId, default = false, associated = true),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `find all mfa enrollments for an account lists only its not associated devices`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns mfaDeviceId
        every { mfaAccountMethodsRepository.findAll(userName) } returns listOf(
            associatedDefaultDevice,
            associatedOtherDevice,
            pendingDevice
        )

        val actual = uut.getEnrollmentsFor(userName, MfaDeviceAssociationFilter.NOT_ASSOCIATED)

        val expected = listOf(
            MfaDevice(userName, EMAIL_MFA_METHOD, pendingEmail, pendingMfaDeviceId, default = false, associated = false),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `find all mfa enrollments for an account lists associated and not associated devices`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns mfaDeviceId
        every { mfaAccountMethodsRepository.findAll(userName) } returns listOf(
            associatedDefaultDevice,
            associatedOtherDevice,
            pendingDevice
        )

        val actual = uut.getEnrollmentsFor(userName, MfaDeviceAssociationFilter.ALL)

        val expected = listOf(
            MfaDevice(userName, EMAIL_MFA_METHOD, email, mfaDeviceId, default = true, associated = true),
            MfaDevice(userName, EMAIL_MFA_METHOD, anotherEmail, anotherMfaDeviceId, default = false, associated = true),
            MfaDevice(userName, EMAIL_MFA_METHOD, pendingEmail, pendingMfaDeviceId, default = false, associated = false),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `find all mfa enrollments for an account without a default device still lists its devices`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns null
        every { mfaAccountMethodsRepository.findAll(userName) } returns listOf(associatedDefaultDevice)

        val actual = uut.getEnrollmentsFor(userName)

        val expected = listOf(
            MfaDevice(userName, EMAIL_MFA_METHOD, email, mfaDeviceId, default = false, associated = true),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `find all mfa enrollments for an account with masked sensible information`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns mfaDeviceId
        every { mfaAccountMethodsRepository.findAll(userName) } returns listOf(
            associatedDefaultDevice,
            associatedOtherDevice
        )
        every { sensitiveEmailMasker.mask(userName) } returns "masked_$userName"
        every { sensitiveEmailMasker.mask(email) } returns "masked_$email"
        every { sensitiveEmailMasker.mask(anotherEmail) } returns "masked_$anotherEmail"

        val actual = uut.getEnrollmentsFor(userName, withMaskedSensibleInformation = true)

        val expected = listOf(
            MfaDevice("masked_$userName", EMAIL_MFA_METHOD, "masked_$email", mfaDeviceId, default = true, associated = true),
            MfaDevice("masked_$userName", EMAIL_MFA_METHOD, "masked_$anotherEmail", anotherMfaDeviceId, default = false, associated = true),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `an already verified email is associated as the default device`() {
        val emailDevice = associatedMfaAccountMethod(userName, userName, EMAIL_MFA_METHOD)
        every { accountRepository.accountFor(userName) } returns account
        every { mfaAccountMethodsRepository.save(userName, EMAIL_MFA_METHOD, userName, true) } returns emailDevice
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns null
        every { mfaAccountMethodsRepository.setAsDefault(userName, emailDevice.mfaDeviceId) } just runs

        uut.associateVerifiedEmail(userName)

        verify { mfaAccountMethodsRepository.save(userName, EMAIL_MFA_METHOD, userName, true) }
        verify { mfaAccountMethodsRepository.setAsDefault(userName, emailDevice.mfaDeviceId) }
    }

    @Test
    fun `an already verified email does not replace the default device of the account`() {
        val emailDevice = associatedMfaAccountMethod(userName, userName, EMAIL_MFA_METHOD)
        every { accountRepository.accountFor(userName) } returns account
        every { mfaAccountMethodsRepository.save(userName, EMAIL_MFA_METHOD, userName, true) } returns emailDevice
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns anotherMfaDeviceId

        uut.associateVerifiedEmail(userName)

        verify(exactly = 0) { mfaAccountMethodsRepository.setAsDefault(any(), any()) }
    }

    @Test
    fun `an already verified email of a missing account is not associated`() {
        every { accountRepository.accountFor(userName) } returns null

        assertThrows(AccountNotFoundException::class.java) {
            uut.associateVerifiedEmail(userName)
        }

        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when the channel to enroll has an associated device the enrolment is refused`() {
        every { accountRepository.accountFor(userName) } returns account
        every { mfaAccountMethodsRepository.findBy(userName, EMAIL_MFA_METHOD, email) } returns
                emailMfaAccountMethod.copy(associated = true)

        val failure = assertThrows(MfaAssociationFailureException::class.java) {
            uut.enroll(userName, EMAIL_MFA_METHOD, email, clientAppId, true)
        }

        assertEquals(MfaAssociationFailureReason.ALREADY_ASSOCIATED, failure.reason)
        verify(exactly = 0) { mfaSender.sendMfaChallengeFor(any(), any()) }
        verify(exactly = 0) { ticketCreator.createTicketFor(any(), any(), any()) }
    }

    @Test
    fun `when the email is enrolled for verification the ticket is a self association email verification ticket`() {
        every { accountRepository.accountFor(userName) } returns account
        every { mfaAccountMethodsRepository.findBy(userName, EMAIL_MFA_METHOD, userName) } returns null
        every { mfaAccountMethodsRepository.save(userName, EMAIL_MFA_METHOD, userName, false) } returns
                notAssociatedMfaAccountMethod(userName, userName, EMAIL_MFA_METHOD)
        every {
            ticketCreator.createTicketFor(account, ClientAppId.empty(), emailVerificationTicketContext())
        } returns ticketId

        val actual = uut.enrollForEmailVerification(userName)

        assertEquals(ticketId, actual)
        verify(exactly = 0) { mfaSender.sendMfaChallengeFor(any(), any()) }
        verify(exactly = 0) { ticketRepository.deleteMfaEnrollmentTicketsFor(any(), any()) }
    }

    @Test
    fun `when the email enrolled for verification has an associated device the device is reused`() {
        every { accountRepository.accountFor(userName) } returns account
        every { mfaAccountMethodsRepository.findBy(userName, EMAIL_MFA_METHOD, userName) } returns
                associatedMfaAccountMethod(userName, userName, EMAIL_MFA_METHOD)
        every {
            ticketCreator.createTicketFor(account, ClientAppId.empty(), emailVerificationTicketContext())
        } returns ticketId

        val actual = uut.enrollForEmailVerification(userName)

        assertEquals(ticketId, actual)
        verify(exactly = 0) { mfaAccountMethodsRepository.save(any(), any(), any(), any()) }
    }

    @Test
    fun `when a channel is enrolled again the pending enrolments of its device are replaced`() {
        every { accountRepository.accountFor(userName) } returns account
        every { mfaAccountMethodsRepository.findBy(userName, EMAIL_MFA_METHOD, email) } returns emailMfaAccountMethod
        every { ticketRepository.deleteMfaEnrollmentTicketsFor(userName, mfaDeviceId) } just runs
        every { ticketCreator.createTicketFor(account, clientAppId, ticketContext(email, mfaDeviceId = mfaDeviceId.content)) } returns ticketId

        uut.enroll(userName, EMAIL_MFA_METHOD, email, clientAppId, false)

        verifyOrder {
            ticketRepository.deleteMfaEnrollmentTicketsFor(userName, mfaDeviceId)
            ticketCreator.createTicketFor(account, clientAppId, ticketContext(email, mfaDeviceId = mfaDeviceId.content))
        }
    }

    private fun emailVerificationTicketContext() = ticketContext(
        userName,
        Ticket.MFA_SELF_ASSOCIATION_CONTEXT_VALUE,
        mfaDeviceId.content,
        Ticket.EMAIL_VERIFICATION_PURPOSE_CONTEXT_VALUE
    )
}
