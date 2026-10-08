package com.vauthenticator.server.support

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleInterval
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import java.time.LocalDateTime

val passwordLifeCycleRule = PasswordLifeCycleRule(
    userName = EMAIL,
    interval = PasswordLifeCycleInterval.parse("PT1H"),
    creationDate = LocalDateTime.of(2026, 10, 1, 10, 0, 0),
    lastEvaluationDate = null,
    action = PasswordLifeCycleAction.PASSWORD_RESET
)
