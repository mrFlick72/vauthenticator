package com.vauthenticator.server.password.domain

fun interface PasswordLifeCycleStrategyExecutor {

    fun execute(rule: PasswordLifeCycleRule)

}