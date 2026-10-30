package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.extentions.decoder
import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.mfa.OtpConfigurationProperties
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class TaimosOtpMfaTest {

    val account = anAccount()
    val email = account.email

    @MockK
    lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    @MockK
    lateinit var keyRepository: KeyRepository

    @MockK
    lateinit var keyDecrypter: KeyDecrypter

    private val kid = Kid("A_KID")

    private fun underTest() =
        TaimosOtpMfa(keyDecrypter, keyRepository, mfaAccountMethodsRepository, OtpConfigurationProperties(6, 60))

    private fun givenTheDeviceSecretKey() {
        val key = Key(
            DataKey.from("QV9FTkNSWVBURURfS0VZ", ""),
            MasterKid(""),
            kid,
            true,
            KeyType.SYMMETRIC,
            KeyPurpose.MFA,
            0L
        )
        every { mfaAccountMethodsRepository.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email) } returns
            MfaAccountMethod(email, MfaDeviceId("A_MFA_DEVICE_ID"), kid, MfaMethod.EMAIL_MFA_METHOD, email, true)

        every { keyRepository.keyFor(kid, KeyPurpose.MFA) } returns key
        every { keyDecrypter.decryptKey("QV9FTkNSWVBURURfS0VZ") } returns "QV9ERUNSWVBURURfU1lNTUVUUklDX0tFWQ=="
    }

    @Test
    fun `when generate a secret from account secret key`() {
        givenTheDeviceSecretKey()

        val actual = underTest().generateSecretKeyFor(account, MfaMethod.EMAIL_MFA_METHOD, email)
        val expectedSecret = Hex.encodeHexString(decoder.decode("QV9ERUNSWVBURURfU1lNTUVUUklDX0tFWQ=="))
        assertEquals(MfaSecret(expectedSecret), actual)
    }

    @Test
    fun `when the code does not match the verification fails as a wrong code`() {
        givenTheDeviceSecretKey()
        val underTest = underTest()
        val rightCode = underTest.getTOTPCode(underTest.generateSecretKeyFor(account, MfaMethod.EMAIL_MFA_METHOD, email))
        val wrongCode = MfaChallenge(((rightCode.content().toInt() + 1) % 1_000_000).toString().padStart(6, '0'))

        assertThrows(WrongMfaCodeException::class.java) {
            underTest.verify(account, MfaMethod.EMAIL_MFA_METHOD, email, wrongCode)
        }
    }

    @Test
    fun `when the code is not a number the verification fails as a wrong code`() {
        givenTheDeviceSecretKey()

        assertThrows(WrongMfaCodeException::class.java) {
            underTest().verify(account, MfaMethod.EMAIL_MFA_METHOD, email, MfaChallenge("NOT_A_CODE"))
        }
    }

    @Test
    fun `when the device secret key cannot be found the verification fails but not as a wrong code`() {
        every { mfaAccountMethodsRepository.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email) } returns null

        val failure = assertThrows(MfaException::class.java) {
            underTest().verify(account, MfaMethod.EMAIL_MFA_METHOD, email, MfaChallenge("123456"))
        }

        assertFalse(failure is WrongMfaCodeException)
    }
}
