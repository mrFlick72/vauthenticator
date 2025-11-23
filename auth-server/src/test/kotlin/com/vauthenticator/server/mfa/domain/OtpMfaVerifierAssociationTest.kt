package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.MfaFixture.account
import com.vauthenticator.server.support.MfaFixture.associatedMfaAccountMethod
import com.vauthenticator.server.support.MfaFixture.challenge
import com.vauthenticator.server.support.MfaFixture.email
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
class OtpMfaVerifierAssociationTest {

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
    fun `when not associated mfa verification succeed on association verification`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns notAssociatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) } just runs

        underTest.verifyMfaChallengeToBeAssociatedFor(
            account.email,
            mfaDeviceId,
            challenge
        )
        verify { mfaAccountMethodsRepository.findBy(mfaDeviceId) }
        verify { accountRepository.accountFor(account.email) }
        verify { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) }
    }
    @Test
    fun `when not associated mfa verification fails on association verification`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns notAssociatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) } throws MfaException("invalid code")

        assertThrows(MfaException::class.java) {
            underTest.verifyMfaChallengeToBeAssociatedFor(
                account.email,
                mfaDeviceId,
                challenge
            )
        }
        verify { mfaAccountMethodsRepository.findBy(mfaDeviceId) }
        verify { accountRepository.accountFor(account.email) }
        verify { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) }
    }

    @Test
    fun `when associated mfa verification fails on association verification`() {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) }  returns associatedMfaAccountMethod(
            userName,
            email,
            MfaMethod.EMAIL_MFA_METHOD
        )
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) } throws AssociatedMfaVerificationException("Mfa Challenge verification failed: this mfa method is already associated")

        assertThrows(AssociatedMfaVerificationException::class.java) {
            underTest.verifyMfaChallengeToBeAssociatedFor(
                account.email,
                mfaDeviceId,
                challenge
            )
        }

        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) }
        every { accountRepository.accountFor(account.email) }
        every { otpMfa.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, challenge) }

    }
}