package com.vauthenticator.server.account.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AccountPatternTest {

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "   "])
    fun `a blank pattern is not valid`(pattern: String) {
        assertThrows<InvalidAccountPatternException> { AccountPattern(pattern) }
    }

    @Test
    fun `a pattern is valid`() {
        AccountPattern("*")
        AccountPattern("*@gmail.com")
        AccountPattern("alice@gmail.com")
    }
}
