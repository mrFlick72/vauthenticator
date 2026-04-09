package com.vauthenticator.server.password.adapter.spring

import com.vauthenticator.server.password.domain.CredentialEncodingException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class Argon2VAuthenticatorPasswordEncoderTest {

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `when delegated encoder returns a value`() {
        val underTest = Argon2VAuthenticatorPasswordEncoder(passwordEncoder)

        every { passwordEncoder.encode("secret") } returns "encoded-secret"

        assertEquals("encoded-secret", underTest.encode("secret"))
    }

    @Test
    fun `when delegated encoder returns null`() {
        val underTest = Argon2VAuthenticatorPasswordEncoder(passwordEncoder)

        every { passwordEncoder.encode("secret") } returns null

        val exception = assertThrows(CredentialEncodingException::class.java) {
            underTest.encode("secret")
        }

        assertEquals(
            "Unable to encode credential using Argon2VAuthenticatorPasswordEncoder",
            exception.message
        )
    }
}
