package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mask.SensitiveDataMaskerResolver
import com.vauthenticator.server.mask.SensitiveEmailMasker
import com.vauthenticator.server.mfa.domain.MfaMethod.EMAIL_MFA_METHOD
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.MfaFixture.email
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.MfaFixture.notAssociatedMfaAccountMethod
import com.vauthenticator.server.support.TicketFixture.ticketContext
import com.vauthenticator.server.ticket.domain.TicketCreator
import com.vauthenticator.server.ticket.domain.TicketId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*


@ExtendWith(MockKExtension::class)
class MfaMethodsEnrollmentTest {

    @MockK
    private lateinit var sensitiveEmailMasker: SensitiveEmailMasker

    @MockK
    private lateinit var ticketCreator: TicketCreator

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

    private val defaultMfaDevice = emailMfaAccountMethod.get()
    private val anotherMfaDeviceId = MfaDeviceId("A_NEW_MFA_DEVICE_ID")
    private val anotherEmail = "irrelevant_$email"
    private val anotherMfaDevice = emailMfaAccountMethod.get()
        .copy(mfaDeviceId = anotherMfaDeviceId, mfaChannel = anotherEmail)


    @BeforeEach
    fun setUp() {
        uut = MfaMethodsEnrollment(
            accountRepository,
            ticketCreator,
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
        every { accountRepository.accountFor(userName) } returns Optional.of(account)
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
        } returns Optional.empty()
        every {
            mfaAccountMethodsRepository.save(
                userName,
                EMAIL_MFA_METHOD,
                email,
                false
            )
        } returns emailMfaAccountMethod.get()

        every { accountRepository.accountFor(userName) } returns Optional.of(account)
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
        every { accountRepository.accountFor(userName) } returns Optional.of(account)
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
    fun `find all mfa enrollments for an account`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns Optional.of(mfaDeviceId)
        every {
            mfaAccountMethodsRepository.findAll(userName)
        } returns listOf(
            defaultMfaDevice,
            anotherMfaDevice
        )
        val actual = uut.getEnrollmentsFor(userName)

        val expected = listOf(
            MfaDevice(userName, EMAIL_MFA_METHOD, email, mfaDeviceId, true),
            MfaDevice(userName, EMAIL_MFA_METHOD, anotherEmail, anotherMfaDeviceId, false),
        )

        assertEquals(expected, actual)

        verify { mfaAccountMethodsRepository.findAll(userName) }
    }

    @Test
    fun `find all mfa enrollments for an account with masked sensible information`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns Optional.of(mfaDeviceId)
        every {
            mfaAccountMethodsRepository.findAll(userName)
        } returns listOf(
            defaultMfaDevice,
            anotherMfaDevice
        )
        every { sensitiveEmailMasker.mask(userName) } returns userName
        every { sensitiveEmailMasker.mask(email) } returns email
        every { sensitiveEmailMasker.mask(anotherEmail) } returns anotherEmail
        val actual = uut.getEnrollmentsFor(userName, true)

        val expected = listOf(
            MfaDevice(userName, EMAIL_MFA_METHOD, email, mfaDeviceId, true),
            MfaDevice(userName, EMAIL_MFA_METHOD, anotherEmail, anotherMfaDeviceId, false),
        )

        assertEquals(expected, actual)

        verify { mfaAccountMethodsRepository.findAll(userName) }
        verify { sensitiveEmailMasker.mask(userName) }
        verify { sensitiveEmailMasker.mask(email) }
        verify { sensitiveEmailMasker.mask(anotherEmail) }
    }
}