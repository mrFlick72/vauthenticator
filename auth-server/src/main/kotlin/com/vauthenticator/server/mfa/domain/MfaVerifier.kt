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
        val mfaAccountMethod = accountDeviceFor(userName, mfaDeviceId)
        if (mfaAccountMethod.associated) {
            throw AssociatedMfaVerificationException("Mfa Challenge verification failed: this mfa method is already associated")
        }
        verify(userName, mfaAccountMethod, challenge)
    }

    override fun verifyAssociatedMfaChallengeFor(userName: String, challenge: MfaChallenge) {
        val defaultDevice = mfaAccountMethodsRepository.getDefaultDevice(userName)
            ?: throw MfaDeviceNotFoundException("Mfa Challenge verification failed: no default mfa device")
        verifyAssociatedMfaChallengeFor(userName, defaultDevice, challenge)
    }

    override fun verifyAssociatedMfaChallengeFor(userName: String, mfaDeviceId: MfaDeviceId, challenge: MfaChallenge) {
        val mfaAccountMethod = accountDeviceFor(userName, mfaDeviceId)
        if (!mfaAccountMethod.associated) {
            throw UnAssociatedMfaVerificationException("Mfa Challenge verification failed: this mfa method has to be associated")
        }
        verify(userName, mfaAccountMethod, challenge)
    }

    private fun accountDeviceFor(userName: String, mfaDeviceId: MfaDeviceId): MfaAccountMethod =
        mfaAccountMethodsRepository.findBy(mfaDeviceId)
            ?.takeIf { it.userName == userName }
            ?: throw MfaDeviceNotFoundException("Mfa Challenge verification failed: mfa device not found")

    private fun verify(userName: String, mfaAccountMethod: MfaAccountMethod, challenge: MfaChallenge) {
        val account = accountRepository.accountFor(userName)
            ?: throw MfaException("Mfa Challenge verification failed: account not found")
        otpMfa.verify(account, mfaAccountMethod.mfaMethod, mfaAccountMethod.mfaChannel, challenge)
    }

}
