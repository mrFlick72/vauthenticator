package com.vauthenticator.server.mask

import com.vauthenticator.server.support.AccountTestFixture.anAccount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SensitiveEmailMaskerTest {

    private val accountMail = anAccount().email


    private val uut = SensitiveEmailMasker()

    @Test
    fun `when a sensitive mail is masked`() {
        val expected = "exxxx@domain.com"
        val actual = uut.mask(accountMail)

        assertEquals(expected, actual)
    }
}