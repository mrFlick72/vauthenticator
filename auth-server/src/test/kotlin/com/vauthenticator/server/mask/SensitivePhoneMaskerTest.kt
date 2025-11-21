package com.vauthenticator.server.mask

import com.vauthenticator.server.support.AccountTestFixture.anAccountWithPhoneNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SensitivePhoneMaskerTest {
    private val accountPhone = anAccountWithPhoneNumber().phone.get().formattedPhone()


    private val uut = SensitivePhoneMasker()

    @Test
    fun `when a sensitive phone number is masked`() {
        val expected = "+39 339xxxxx223"
        val actual = uut.mask(accountPhone)

        assertEquals(expected, actual)
    }
}