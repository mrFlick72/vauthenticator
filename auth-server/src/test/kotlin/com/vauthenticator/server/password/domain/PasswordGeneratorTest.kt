package com.vauthenticator.server.password.domain

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private const val STRONG_PASSWORD_LENGTH = 20

class PasswordGeneratorTest {

    private val uut = PasswordGenerator(PasswordGeneratorCriteria(5, 5, 5, 5))


    @Test
    fun name() {

        val actual = uut.generate()
        Assertions.assertEquals(STRONG_PASSWORD_LENGTH, actual.length)
        Assertions.assertTrue(actual.length == STRONG_PASSWORD_LENGTH)
    }

}