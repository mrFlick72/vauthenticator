package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.MfaFixture.account
import com.vauthenticator.server.support.MfaFixture.accountWithPhone
import com.vauthenticator.server.support.MfaFixture.challenge
import com.vauthenticator.server.support.MfaFixture.formattedPhone
import com.vauthenticator.server.support.MfaFixture.keyId
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.MfaFixture.userName
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OtpMfaVerifierUnresolvedDeviceTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    @MockK
    lateinit var otpMfa: OtpMfa

    lateinit var underTest: MfaVerifier

    private val anotherAccountDevice =
        MfaAccountMethod("another@email.com", mfaDeviceId, keyId, MfaMethod.EMAIL_MFA_METHOD, "another@email.com", true)

    @BeforeEach
    fun setUp() {
        underTest = OtpMfaVerifier(accountRepository, otpMfa, mfaAccountMethodsRepository)
        every { mfaAccountMethodsRepository.findBy(any(), any(), any()) } returns null
        every { accountRepository.accountFor(userName) } returns account
    }

    @Test
    fun `when the mfa device is unknown the login challenge is rejected`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns null

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(userName, mfaDeviceId, challenge)
        }
    }

    @Test
    fun `when the mfa device belongs to another account the login challenge is rejected`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns anotherAccountDevice

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(userName, mfaDeviceId, challenge)
        }
        verify(exactly = 0) { otpMfa.verify(any(), any(), any(), any()) }
    }

    @Test
    fun `when the account has no default mfa device the login challenge is rejected`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns null

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(userName, challenge)
        }
    }

    @Test
    fun `when the default mfa device no longer exists the login challenge is rejected`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns mfaDeviceId
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns null

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(userName, challenge)
        }
    }

    @Test
    fun `when the default mfa device belongs to another account the login challenge is rejected`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns mfaDeviceId
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns anotherAccountDevice

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(userName, challenge)
        }
        verify(exactly = 0) { otpMfa.verify(any(), any(), any(), any()) }
    }

    @Test
    fun `when an sms mfa device receives a wrong code the login challenge is rejected`() {
        val phone = formattedPhone!!
        every { accountRepository.accountFor(accountWithPhone.email) } returns accountWithPhone
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns
                MfaAccountMethod(accountWithPhone.email, mfaDeviceId, keyId, MfaMethod.SMS_MFA_METHOD, phone, true)
        every { otpMfa.verify(accountWithPhone, MfaMethod.SMS_MFA_METHOD, phone, challenge) } throws MfaException("wrong code")

        assertThrows(MfaException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(accountWithPhone.email, mfaDeviceId, challenge)
        }
    }

    @Test
    fun `when the mfa device to associate no longer exists the association challenge is rejected`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns null

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyMfaChallengeToBeAssociatedFor(userName, mfaDeviceId, challenge)
        }
    }

    @Test
    fun `when the mfa device to associate belongs to another account the association challenge is rejected`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns anotherAccountDevice.copy(associated = false)

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.verifyMfaChallengeToBeAssociatedFor(userName, mfaDeviceId, challenge)
        }
        verify(exactly = 0) { otpMfa.verify(any(), any(), any(), any()) }
    }
}
