package com.vauthenticator.server.password.domain

import java.time.Duration

data class PasswordLifeCycleRule(val userName : String, val ttl: Duration, val action: PasswordLifeCycleAction)

enum class PasswordLifeCycleAction {
    PASSWORD_RESET, ACCOUNT_LOCK
}
