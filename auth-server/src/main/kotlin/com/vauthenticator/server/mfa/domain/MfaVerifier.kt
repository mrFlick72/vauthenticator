package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.AccountRepository

interface MfaVerifier {
    fun verifyMfaChallengeToBeAssociatedFor(
        userName: String,
        mfaDeviceId: MfaDeviceId,
        challenge: MfaChallenge
    )

    fun verifyAssociatedMfaChallengeFor(
        userName: String,
        challenge: MfaChallenge
    )

    fun verifyAssociatedMfaChallengeFor(
        userName: String,
        mfaDeviceId: MfaDeviceId,
        challenge: MfaChallenge
    )
}

class OtpMfaVerifier(
    private val accountRepository: AccountRepository,
    private val otpMfa: OtpMfa,
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository
) : MfaVerifier {
    override fun verifyMfaChallengeToBeAssociatedFor(
        userName: String,
        mfaDeviceId: MfaDeviceId,
        challenge: MfaChallenge
    ) {
        mfaAccountMethodsRepository.findBy(mfaDeviceId)
            .map { mfaAccountMethod ->
                val account = accountRepository.accountFor(userName).get()
                if (!mfaAccountMethod.associated) {
                    otpMfa.verify(account, mfaAccountMethod.mfaMethod, mfaAccountMethod.mfaChannel, challenge)
                } else {
                    throw AssociatedMfaVerificationException("Mfa Challenge verification failed: this mfa method is already associated")
                }
            }

    }

    private fun verifyAssociatedMfaChallengeFor(
        userName: String,
        mfaMethod: MfaMethod,
        mfaChannel: String,
        challenge: MfaChallenge
    ) {
        mfaAccountMethodsRepository.findBy(userName, MfaMethod.EMAIL_MFA_METHOD, mfaChannel)
            .map {
                val account = accountRepository.accountFor(userName).get()
                if (it.associated) {
                    otpMfa.verify(account, mfaMethod, mfaChannel, challenge)
                } else {
                    throw UnAssociatedMfaVerificationException("Mfa Challenge verification failed: this mfa method has to be associated")
                }
            }
    }

    override fun verifyAssociatedMfaChallengeFor(userName: String, challenge: MfaChallenge) {
        mfaAccountMethodsRepository.getDefaultDevice(userName)
            .flatMap { mfaAccountMethodsRepository.findBy(it) }
            .map {
                verifyAssociatedMfaChallengeFor(
                    userName, it.mfaMethod, it.mfaChannel, challenge
                )
            }
    }

    override fun verifyAssociatedMfaChallengeFor(userName: String, mfaDeviceId: MfaDeviceId, challenge: MfaChallenge) {
        mfaAccountMethodsRepository.findBy(mfaDeviceId)
            .map {
                verifyAssociatedMfaChallengeFor(
                    userName, it.mfaMethod, it.mfaChannel, challenge
                )
            }
    }

}