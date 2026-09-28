package com.vauthenticator.server.password.domain.lifecycle

import java.time.Duration
import java.time.LocalDateTime

data class PasswordLifeCycleRule(val userName : String, val ttl: Duration, val creationDate : LocalDateTime, val lastEvaluationDate : LocalDateTime?, val action: PasswordLifeCycleAction)

enum class PasswordLifeCycleAction {
    PASSWORD_RESET, ACCOUNT_LOCK
}
