package com.vauthenticator.server.support

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import java.time.Duration
import java.time.LocalDateTime

val passwordLifeCycleRule = PasswordLifeCycleRule(
    userName = EMAIL,
    interval = Duration.ofHours(1),
    creationDate = LocalDateTime.of(2026, 10, 1, 10, 0, 0),
    lastEvaluationDate = null,
    action = PasswordLifeCycleAction.PASSWORD_RESET
)
