package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.AccountTestFixture.anAccountWithPhoneNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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

    @Test
    fun `missing mfa code makes sms context invalid`() {
        val account = anAccountWithPhoneNumber()
        val uut = SimpleSmsMessageFactory()

        val actual = assertThrows(InvalidSmsMessageContextException::class.java) {
            uut.makeSmsMessageFor(account, emptyMap())
        }

        assertEquals("The sms context is not valid, missing mfaCode", actual.message)
    }

    @Test
    fun `blank phone makes sms context invalid`() {
        val account = anAccount()
        val uut = SimpleSmsMessageFactory()

        val actual = assertThrows(InvalidSmsMessageContextException::class.java) {
            uut.makeSmsMessageFor(account, mapOf("mfaCode" to "123"))
        }

        assertEquals("The sms context is not valid, blank phone", actual.message)
    }

    @Test
    fun `mfa code must be a string`() {
        val account = anAccountWithPhoneNumber()
        val uut = SimpleSmsMessageFactory()

        val actual = assertThrows(InvalidSmsMessageContextException::class.java) {
            uut.makeSmsMessageFor(account, mapOf("mfaCode" to 123))
        }

        assertEquals("The sms context is not valid, mfaCode must be a String", actual.message)
    }
}
