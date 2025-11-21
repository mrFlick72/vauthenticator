package com.vauthenticator.server.keys.adapter.java

import com.vauthenticator.server.extentions.decoder
import com.vauthenticator.server.support.KeysUtils
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@ExtendWith(MockKExtension::class)
class JavaSecurityCryptographicOperationsTest {

    @MockK
    lateinit var repository: KeyGeneratorMasterKeyRepository

    lateinit var uut: JavaSecurityCryptographicOperations

    @BeforeEach
    fun setUp() {
        uut = JavaSecurityCryptographicOperations(repository)
    }

    @Test
    fun `when a new rsa key pair is created`() {
        mockkStatic(KeyPairGenerator::class)
        val expected = mockk<KeyPair>()
        val generator = mockk<KeyPairGenerator>(relaxed = true)
        every { KeyPairGenerator.getInstance("RSA", "BC") } returns generator
        every { generator.initialize(RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4)) } just runs
        every { generator.generateKeyPair() } returns expected

        val actual = uut.generateRSAKeyPair()
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `when an encoded plain text is encrypted with some master key`() {
        val expected = "ENCRYPTED_DATA".toByteArray()
        val encodedPlainText = "INPUT_TEXT".toByteArray()
        val masterKeyValue = "QV9LRVk="
        val key = SecretKeySpec(decoder.decode(masterKeyValue), "AES")
        val cipher = mockk<Cipher>(relaxed = true)
        mockkStatic(Cipher::class)

        every { repository.maskerKeyFor(KeysUtils.aMasterKey) } returns masterKeyValue
        every { Cipher.getInstance("AES") } returns cipher
        every { cipher.init(Cipher.ENCRYPT_MODE, key) } just runs
        every { cipher.doFinal(encodedPlainText) } returns expected

        val actual = uut.encryptKeyWith(KeysUtils.aMasterKey, encodedPlainText)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `when an encoded encrypted text is decrypted with some master key`() {
        val expected = "DECRYPTED_DATA".toByteArray()
        val encodedEncryptedText = "RU5DUllQVEVEX0lOUFVUX1RFWFQ=".toByteArray()
        val masterKeyValue = "QV9LRVk="
        val key = SecretKeySpec(decoder.decode(masterKeyValue), "AES")
        val cipher = mockk<Cipher>(relaxed = true)
        mockkStatic(Cipher::class)

        every { repository.maskerKeyFor(KeysUtils.aMasterKey) } returns masterKeyValue
        every { Cipher.getInstance("AES") } returns cipher
        every { cipher.init(Cipher.DECRYPT_MODE, key) } just runs
        every { cipher.doFinal(decoder.decode(encodedEncryptedText)) } returns expected

        val actual = uut.decryptKeyWith(KeysUtils.aMasterKey, encodedEncryptedText)
        Assertions.assertEquals(expected, actual)
    }


}