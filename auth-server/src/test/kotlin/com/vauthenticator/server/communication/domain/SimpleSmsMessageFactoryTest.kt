package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.support.AccountTestFixture.anAccountWithPhoneNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SimpleSmsMessageFactoryTest {

    @Test
    fun `happy path`() {
        val account = anAccountWithPhoneNumber()
        val uut = SimpleSmsMessageFactory()

        val expected = SmsMessage("+39 339 2323223", "123")
        val actual = uut.makeSmsMessageFor(account, mapOf("mfaCode" to "123"))

        assertEquals(expected, actual)
    }
}