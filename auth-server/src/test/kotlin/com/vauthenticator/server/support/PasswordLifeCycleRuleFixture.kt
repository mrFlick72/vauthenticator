package com.vauthenticator.server.support

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import java.time.Duration

val passwordLifeCycleRule = PasswordLifeCycleRule(
    userName = EMAIL,
    ttl = Duration.ofHours(1),
    action = PasswordLifeCycleAction.PASSWORD_RESET
)