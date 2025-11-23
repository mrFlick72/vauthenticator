package com.vauthenticator.server.mfa.domain

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil
import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.extentions.decoder
import com.vauthenticator.server.keys.domain.KeyDecrypter
import com.vauthenticator.server.keys.domain.KeyPurpose
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.mfa.OtpConfigurationProperties
import org.apache.commons.codec.binary.Hex

interface OtpMfa {
    fun generateSecretKeyFor(account: Account, mfaMethod: MfaMethod, mfaChannel: String): MfaSecret
    fun getTOTPCode(secretKey: MfaSecret): MfaChallenge
    fun verify(account: Account, mfaMethod: MfaMethod, mfaChannel: String, optCode: MfaChallenge)
}

class TaimosOtpMfa(
    private val keyDecrypter: KeyDecrypter,
    private val keyRepository: KeyRepository,
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    private val properties: OtpConfigurationProperties
) : OtpMfa {
    private val tokenTimeWindow: Int = properties.timeToLiveInSeconds
    private val tokenTimeWindowMillis: Long = (tokenTimeWindow * 1000).toLong()

    override fun generateSecretKeyFor(account: Account, mfaMethod: MfaMethod, mfaChannel: String): MfaSecret {
        val mfaAccountMethod =
            mfaAccountMethodsRepository.findBy(account.email, mfaMethod, mfaChannel)
                .orElseGet { null }
        val encryptedSecret = keyRepository.keyFor(mfaAccountMethod.key, KeyPurpose.MFA)
        val decryptKeyAsByteArray = keyDecrypter.decryptKey(encryptedSecret.dataKey.encryptedPrivateKeyAsString())
        val decryptedKey = Hex.encodeHexString(decoder.decode(decryptKeyAsByteArray))
        return MfaSecret(decryptedKey)
    }

    override fun getTOTPCode(secretKey: MfaSecret): MfaChallenge {
        return MfaChallenge(
            TimeBasedOneTimePasswordUtil.generateNumberStringHex(
                secretKey.content(),
                System.currentTimeMillis(),
                tokenTimeWindow,
                properties.length
            )
        )
    }

    override fun verify(account: Account, mfaMethod: MfaMethod, mfaChannel: String, optCode: MfaChallenge) {
        val mfaSecret = generateSecretKeyFor(account, mfaMethod, mfaChannel)
        try {
            val validated =
                TimeBasedOneTimePasswordUtil.validateCurrentNumberHex(
                    mfaSecret.content(),
                    optCode.content().toInt(),
                    tokenTimeWindowMillis,
                    System.currentTimeMillis(),
                    tokenTimeWindow,
                    properties.length
                )
            if (!validated) {
                throw MfaException("Customer Code does not match with system code")
            }
        } catch (e: RuntimeException) {
            throw MfaException("Customer Code does not match with system code")
        }
    }

}

