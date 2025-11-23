package com.vauthenticator.server.keys.adapter.java

import com.vauthenticator.server.extentions.encoder
import com.vauthenticator.server.keys.domain.MasterKid
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class JavaSecurityKeyDecrypterTest {

    @MockK
    lateinit var javaSecurityCryptographicOperations: JavaSecurityCryptographicOperations

    @Test
    fun `happy path`() {
        val encrypted = "AN_ENCRYPTED_VALUE"
        val decrypted = "AN_UNENCRYPTED_VALUE".toByteArray()
        val maserKid = "A_MASTER_KEY"

        val uut = JavaSecurityKeyDecrypter(maserKid, javaSecurityCryptographicOperations)

        every { javaSecurityCryptographicOperations.decryptKeyWith(MasterKid(maserKid), encrypted.toByteArray()) } returns decrypted

        val actual = uut.decryptKey(encrypted)
        val expected = encoder.encode(decrypted).decodeToString()

        Assertions.assertEquals(expected, actual)
    }
}