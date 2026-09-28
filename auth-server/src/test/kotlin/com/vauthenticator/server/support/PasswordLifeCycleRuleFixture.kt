package com.vauthenticator.server.support

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import java.time.Duration
import java.time.LocalDateTime

val passwordLifeCycleRule = PasswordLifeCycleRule(
    userName = EMAIL,
    ttl = Duration.ofHours(1),
    creationDate = LocalDateTime.now(),
    lastEvaluationDate = null,
    action = PasswordLifeCycleAction.PASSWORD_RESET
)