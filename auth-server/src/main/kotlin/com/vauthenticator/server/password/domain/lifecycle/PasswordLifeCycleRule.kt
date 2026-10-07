package com.vauthenticator.server.password.domain.lifecycle

import java.time.Duration
import java.time.LocalDateTime

data class PasswordLifeCycleRule(
    val userName: String,
    val interval: Duration,
    val creationDate: LocalDateTime,
    val lastEvaluationDate: LocalDateTime?,
    val action: PasswordLifeCycleAction
) {
    fun nextEvaluationDate(): LocalDateTime = (lastEvaluationDate ?: creationDate).plus(interval)

    fun isDue(now: LocalDateTime): Boolean = now.isAfter(nextEvaluationDate())
}

enum class PasswordLifeCycleAction(val oneShot: Boolean) {
    PASSWORD_RESET(oneShot = false), ACCOUNT_LOCK(oneShot = true)
}

class InvalidPasswordLifeCycleIntervalException(message: String) : RuntimeException(message)
