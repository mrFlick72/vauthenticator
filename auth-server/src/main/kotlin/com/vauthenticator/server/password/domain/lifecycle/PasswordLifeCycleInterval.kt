package com.vauthenticator.server.password.domain.lifecycle

import java.time.Duration
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeParseException

/**
 * A positive ISO-8601 duration in the PnYnMnWnDTnHnMnS form. Years, months, weeks and days are calendar based,
 * hours, minutes and seconds are exact.
 */
class PasswordLifeCycleInterval private constructor(
    private val text: String,
    private val calendarPart: Period,
    private val timePart: Duration
) {

    fun addTo(date: LocalDateTime): LocalDateTime = date.plus(calendarPart).plus(timePart)

    override fun toString(): String = text

    override fun equals(other: Any?): Boolean = other is PasswordLifeCycleInterval && text == other.text

    override fun hashCode(): Int = text.hashCode()

    companion object {
        fun parse(text: String): PasswordLifeCycleInterval {
            if (!text.startsWith("P")) {
                throw notAnInterval(text)
            }
            val timeSeparator = text.indexOf('T')
            val calendarText = if (timeSeparator < 0) text else text.substring(0, timeSeparator)
            val timeText = if (timeSeparator < 0) null else "P" + text.substring(timeSeparator)

            val (calendarPart, timePart) = try {
                val calendarPart = if (calendarText == "P") Period.ZERO else Period.parse(calendarText)
                val timePart = timeText?.let { Duration.parse(it) } ?: Duration.ZERO
                calendarPart to timePart
            } catch (e: DateTimeParseException) {
                throw notAnInterval(text)
            }

            if (calendarText == "P" && timeText == null) {
                throw notAnInterval(text)
            }
            if (calendarPart.isNegative || timePart.isNegative || (calendarPart.isZero && timePart.isZero)) {
                throw InvalidPasswordLifeCycleIntervalException("The interval must be positive: $text")
            }
            return PasswordLifeCycleInterval(text, calendarPart, timePart)
        }

        private fun notAnInterval(text: String) =
            InvalidPasswordLifeCycleIntervalException("The interval must be an ISO-8601 duration: $text")
    }
}
