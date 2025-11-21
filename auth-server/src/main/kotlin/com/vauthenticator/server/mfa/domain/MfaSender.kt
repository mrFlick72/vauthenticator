package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.communication.domain.SmsSenderService


class MfaChallengeSender(
    private val accountRepository: AccountRepository,
    private val otpMfa: OtpMfa,
    private val mfaMailSender: EMailSenderService,
    private val smsSenderService: SmsSenderService,
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository
) {

    fun sendMfaChallengeFor(userName: String, mfaDeviceId: MfaDeviceId) {
        mfaAccountMethodsRepository.findBy(mfaDeviceId)
            .map {
                sendMfaChallengeFor(
                    userName,
                    it.mfaMethod,
                    it.mfaChannel
                )
            }
    }


    fun sendMfaChallengeFor(userName: String) {
        mfaAccountMethodsRepository.getDefaultDevice(userName)
            .flatMap { mfaAccountMethodsRepository.findBy(it) }
            .map {
                sendMfaChallengeFor(
                    userName,
                    it.mfaMethod,
                    it.mfaChannel
                )
            }
    }

    private fun sendMfaChallengeFor(userName: String, mfaMethod: MfaMethod, mfaChannel: String) {
        val account = accountRepository.accountFor(userName).get()
        val mfaSecret = otpMfa.generateSecretKeyFor(account, mfaMethod, mfaChannel)
        val mfaCode = otpMfa.getTOTPCode(mfaSecret).content()

        when (mfaMethod) {
            MfaMethod.EMAIL_MFA_METHOD -> {
                mfaMailSender.sendFor(account, mapOf("email" to mfaChannel, "mfaCode" to mfaCode))
            }

            MfaMethod.SMS_MFA_METHOD -> {
                smsSenderService.sendFor(account, mapOf("phone" to mfaChannel, "mfaCode" to mfaCode))
            }
        }

    }
}