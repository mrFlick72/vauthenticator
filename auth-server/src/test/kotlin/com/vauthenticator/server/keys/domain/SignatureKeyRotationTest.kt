package com.vauthenticator.server.keys.domain

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(MockKExtension::class)
class SignatureKeyRotationTest {

    @MockK
    private lateinit var keyRepository: KeyRepository

    private val kid = Kid("A_KID")
    private val ttl = Duration.ofSeconds(100)
    private val expected = "expected_kid"
    private val masterKid = MasterKid("A_MASER_KID")

    @Test
    fun `when the key is rotated`() {
        val uut = SignatureKeyRotation(keyRepository)

        every { keyRepository.deleteKeyFor(kid, KeyPurpose.SIGNATURE, ttl) } just runs
        every { keyRepository.createKeyFrom(masterKid) } returns Kid(expected)

        val actual: Kid = uut.rotate(masterKid, kid, ttl)

        verify { keyRepository.deleteKeyFor(kid, KeyPurpose.SIGNATURE, ttl) }
        verify { keyRepository.createKeyFrom(masterKid) }

        assertEquals(expected, actual.content())
    }
}