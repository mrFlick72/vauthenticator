package com.vauthenticator.server.extentions

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class ToSha256Test {

    @Test
    fun `should return expected sha-256 for abc`() {
        val actual = "abc".toSha256()

        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            actual
        )
    }

    @Test
    fun `should return expected sha-256 for empty string`() {
        val actual = "".toSha256()

        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            actual
        )
    }

    @Test
    fun `should preserve leading zeroes in hex output`() {
        val actual = "39".toSha256()

        assertEquals(64, actual.length)
        assertEquals(
            "0b918943df0962bc7a1824c0555a389347b4febdc7cf9d1254406d80ce44e3f9",
            actual
        )
    }

    @Test
    fun `when MessageDigest throws with message then Sha255CodingException preserves message`() {
        withMockedMessageDigest {
            every { MessageDigest.getInstance("SHA-256") } throws NoSuchAlgorithmException("SHA-256 not available")

            val actual = assertThrows(Sha255CodingException::class.java) {
                "abc".toSha256()
            }

            assertEquals("SHA-256 not available", actual.message)
        }
    }

    @Test
    fun `when MessageDigest throws without message then Sha255CodingException uses default message`() {
        withMockedMessageDigest {
            every { MessageDigest.getInstance("SHA-256") } throws NoSuchAlgorithmException()

            val actual = assertThrows(Sha255CodingException::class.java) {
                "abc".toSha256()
            }

            assertEquals("Error while encoding string to sha-256", actual.message)
        }
    }

    private inline fun <T> withMockedMessageDigest(block: () -> T): T {
        mockkStatic(MessageDigest::class)

        return try {
            block()
        } finally {
            unmockkStatic(MessageDigest::class)
        }
    }
}
