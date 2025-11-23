package com.vauthenticator.server.keys.adapter.java

import com.vauthenticator.server.extentions.encoder
import com.vauthenticator.server.keys.domain.DataKey
import com.vauthenticator.server.keys.domain.MasterKid
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

@ExtendWith(MockKExtension::class)
class JavaSecurityKeyGeneratorTest {

    @MockK
    lateinit var javaSecurityCryptographicOperations: JavaSecurityCryptographicOperations

    private val masterKid = MasterKid("A_MASTER_KEY")
    private val anEncryptedPrivateKEyValueAsByteArray = "AN_ENCRYPTED_PRIVATE_KEY_VALUE".toByteArray()
    private val aPublicKeyValueAsByteArray = "A_PUBLIC_KEY_VALUE".toByteArray()

    lateinit var uut: JavaSecurityKeyGenerator

    @BeforeEach
    fun setUp() {
        uut = JavaSecurityKeyGenerator(javaSecurityCryptographicOperations)
    }

    @Test
    fun `when a new data key is created`() {
        val keyPair = mockk<KeyPair>()
        val privateKey = mockk<PrivateKey>()

        every { javaSecurityCryptographicOperations.generateRSAKeyPair() } returns keyPair
        every { keyPair.private } returns privateKey
        every { privateKey.encoded } returns anEncryptedPrivateKEyValueAsByteArray

        every {
            javaSecurityCryptographicOperations.encryptKeyWith(
                masterKid,
                anEncryptedPrivateKEyValueAsByteArray
            )
        } returns anEncryptedPrivateKEyValueAsByteArray

        val actual = uut.dataKeyFor(masterKid)
        val expected = DataKey.from(encoder.encode(anEncryptedPrivateKEyValueAsByteArray).decodeToString(), "")
        Assertions.assertEquals(expected, actual)
    }


    @Test
    fun `when a new data key pair is created`() {
        val keyPair = mockk<KeyPair>()
        val privateKey = mockk<PrivateKey>()
        val publicKey = mockk<PublicKey>()

        every { javaSecurityCryptographicOperations.generateRSAKeyPair() } returns keyPair
        every { keyPair.private } returns privateKey
        every { privateKey.encoded } returns anEncryptedPrivateKEyValueAsByteArray

        every { keyPair.public } returns publicKey
        every { publicKey.encoded } returns aPublicKeyValueAsByteArray

        every {
            javaSecurityCryptographicOperations.encryptKeyWith(
                masterKid,
                anEncryptedPrivateKEyValueAsByteArray
            )
        } returns anEncryptedPrivateKEyValueAsByteArray

        val actual = uut.dataKeyPairFor(masterKid)
        val expected = DataKey.from(
            encoder.encode(anEncryptedPrivateKEyValueAsByteArray).decodeToString(),
            encoder.encode(aPublicKeyValueAsByteArray).decodeToString()
        )
        Assertions.assertEquals(expected, actual)
    }
}