package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.MfaFixture.account
import com.vauthenticator.server.support.MfaFixture.associatedMfaAccountMethod
import com.vauthenticator.server.support.MfaFixture.challenge
import com.vauthenticator.server.support.MfaFixture.email
import com.vauthenticator.server.support.MfaFixture.keyId
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.MfaFixture.notAssociatedMfaAccountMethod
import com.vauthenticator.server.support.MfaFixture.userName
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

@ExtendWith(MockKExtension::class)
class OtpMfaVerifierWithDefaultMfaDeviceTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    @MockK
    lateinit var otpMfa: OtpMfa

    lateinit var underTest: MfaVerifier

    @BeforeEach
    fun setUp() {
        underTest = OtpMfaVerifier(accountRepository, otpMfa, mfaAccountMethodsRepository)
    }

    @Test
    fun `when associated mfa challenge is successfully verified with default mfa device`() {
        val challenge = MfaChallenge("AN_MFA_CHALLENGE")

        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns Optional.of(mfaDeviceId)
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns Optional.of(
            MfaAccountMethod(
                userName,
                mfaDeviceId, keyId, MfaMethod.EMAIL_MFA_METHOD, email, true
            )
        )
        every { mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, email) } returns associatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        every { accountRepository.accountFor(userName) } returns Optional.of(account)
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) } just runs

        underTest.verifyAssociatedMfaChallengeFor(userName, challenge)

        verify { mfaAccountMethodsRepository.getDefaultDevice(userName) }
        verify { mfaAccountMethodsRepository.findBy(mfaDeviceId) }
        verify { mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, email) }
        verify { accountRepository.accountFor(userName) }
        verify { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) }

    }

    @Test
    fun `when not associated mfa challenge fails on verification with default mfa device`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns Optional.of(mfaDeviceId)
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns Optional.of(
            MfaAccountMethod(
                userName,
                mfaDeviceId, keyId, MfaMethod.EMAIL_MFA_METHOD, email, false
            )
        )
        every { mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, email) } returns notAssociatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        every { accountRepository.accountFor(userName) } returns Optional.of(account)
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) } just runs

        assertThrows(UnAssociatedMfaVerificationException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(
                userName,
                challenge
            )
        }

        verify { mfaAccountMethodsRepository.getDefaultDevice(userName) }
        verify { mfaAccountMethodsRepository.findBy(mfaDeviceId) }
        verify { mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, email) }
        verify { accountRepository.accountFor(userName) }
    }

    @Test
    fun `when associated mfa challenge fails on verification with default mfa device`() {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns Optional.of(mfaDeviceId)
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns associatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) } throws MfaException("")
        every { mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, email) } returns associatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        assertThrows(MfaException::class.java) {
            underTest.verifyAssociatedMfaChallengeFor(
                userName,
                challenge
            )
        }

        verify { mfaAccountMethodsRepository.getDefaultDevice(userName) }
        verify { mfaAccountMethodsRepository.findBy(mfaDeviceId) }
        verify { accountRepository.accountFor(account.email) }
        verify { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) }
        verify { mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, email) }
    }
}