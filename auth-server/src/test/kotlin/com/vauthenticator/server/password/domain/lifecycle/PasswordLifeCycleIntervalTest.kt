package com.vauthenticator.server.password.domain.lifecycle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime

class PasswordLifeCycleIntervalTest {

    private val start = LocalDateTime.of(2026, 10, 7, 10, 0, 0)

    @ParameterizedTest
    @CsvSource(
        "P3M,         2027-01-07T10:00",
        "P1Y,         2027-10-07T10:00",
        "P10Y,        2036-10-07T10:00",
        "P1Y6M,       2028-04-07T10:00",
        "P2W,         2026-10-21T10:00",
        "P90D,        2027-01-05T10:00",
        "PT12H,       2026-10-07T22:00",
        "PT90M,       2026-10-07T11:30",
        "PT0.5S,      2026-10-07T10:00:00.500",
        "P1MT12H,     2026-11-07T22:00",
        "P1Y2M3W4DT5H6M7S, 2028-01-01T15:06:07",
    )
    fun `when an ISO-8601 interval is added to a date`(text: String, expected: String) {
        assertEquals(LocalDateTime.parse(expected), PasswordLifeCycleInterval.parse(text).addTo(start))
    }

    @Test
    fun `months are calendar months`() {
        val endOfJanuary = LocalDateTime.of(2027, 1, 31, 10, 0, 0)

        assertEquals(LocalDateTime.of(2027, 2, 28, 10, 0, 0), PasswordLifeCycleInterval.parse("P1M").addTo(endOfJanuary))
    }

    @Test
    fun `the interval keeps the text it was expressed with`() {
        assertEquals("P3M", PasswordLifeCycleInterval.parse("P3M").toString())
        assertEquals("P1MT12H", PasswordLifeCycleInterval.parse("P1MT12H").toString())
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "", "P", "PT", "3M", "90 days", "7776000", "P0D", "PT0S", "P0DT0S",
            "-P1M", "P-1M", "PT-1H", "P1M-1D", "P1MT-1H", "P1MT", "P1H", "PT1D", "P1.5M"
        ]
    )
    fun `when an interval is not a positive ISO-8601 duration`(text: String) {
        assertThrows<InvalidPasswordLifeCycleIntervalException> { PasswordLifeCycleInterval.parse(text) }
    }
}
