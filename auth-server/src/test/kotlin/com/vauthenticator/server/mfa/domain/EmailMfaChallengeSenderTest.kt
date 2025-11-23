package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.communication.domain.SmsSenderService
import com.vauthenticator.server.support.MfaFixture.account
import com.vauthenticator.server.support.MfaFixture.accountWithPhone
import com.vauthenticator.server.support.MfaFixture.associatedMfaAccountMethod
import com.vauthenticator.server.support.MfaFixture.challenge
import com.vauthenticator.server.support.MfaFixture.email
import com.vauthenticator.server.support.MfaFixture.formattedPhone
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.MfaFixture.mfaSecret
import com.vauthenticator.server.support.MfaFixture.userName
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream


@ExtendWith(MockKExtension::class)
class MfaChallengeSenderTest {

    @MockK
    lateinit var otp: OtpMfa

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var emailSenderService: EMailSenderService

    @MockK
    lateinit var smsSenderService: SmsSenderService

    @MockK
    lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    lateinit var uut: MfaChallengeSender

    @BeforeEach
    fun setUp() {
        uut = MfaChallengeSender(
            accountRepository,
            otp,
            emailSenderService,
            smsSenderService,
            mfaAccountMethodsRepository
        )
    }

    @ParameterizedTest
    @MethodSource("testArgumentProvider")
    fun `when a otp is sent via a defined mfa device`(
        mfaChannel: String,
        mfaMethod: MfaMethod,
        testAccount: Account
    ) {
        testSetup(mfaChannel, mfaMethod, testAccount)

        uut.sendMfaChallengeFor(userName, mfaDeviceId)
    }

    @ParameterizedTest
    @MethodSource("testArgumentProvider")
    fun `when a otp is sent via the default mfa enrolled device`(
        mfaChannel: String,
        mfaMethod: MfaMethod,
        testAccount: Account
    ) {
        every { mfaAccountMethodsRepository.getDefaultDevice(userName) } returns Optional.of(mfaDeviceId)
        testSetup(mfaChannel, mfaMethod, testAccount)

        uut.sendMfaChallengeFor(userName)
    }

    private fun testSetup(
        mfaChannel: String,
        mfaMethod: MfaMethod,
        testAccount: Account
    ) {
        every { mfaAccountMethodsRepository.findBy(mfaDeviceId) } returns associatedMfaAccountMethod(
            userName,
            mfaChannel,
            mfaMethod
        )
        every { accountRepository.accountFor(userName) } returns Optional.of(testAccount)
        every { otp.generateSecretKeyFor(testAccount, mfaMethod, mfaChannel) } returns mfaSecret
        every { otp.getTOTPCode(mfaSecret) } returns challenge

        every {
            smsSenderService.sendFor(
                testAccount,
                mapOf("phone" to mfaChannel, "mfaCode" to challenge.content())
            )
        } just runs
        every {
            emailSenderService.sendFor(
                testAccount,
                mapOf("email" to mfaChannel, "mfaCode" to challenge.content())
            )
        } just runs
    }

    companion object {
        @JvmStatic
        private fun testArgumentProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(email, MfaMethod.EMAIL_MFA_METHOD, account),
                Arguments.of(formattedPhone, MfaMethod.SMS_MFA_METHOD, accountWithPhone),
            )
        }
    }

}