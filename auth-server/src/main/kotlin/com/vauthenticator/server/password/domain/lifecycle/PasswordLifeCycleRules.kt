package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import java.time.Clock
import java.time.DateTimeException
import java.time.Duration
import java.time.LocalDateTime

class PasswordLifeCycleRules(
    private val clock: Clock,
    private val accountRepository: AccountRepository,
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository
) {

    fun register(userName: String, action: PasswordLifeCycleAction, interval: Duration) {
        if (interval.isZero || interval.isNegative) {
            throw InvalidPasswordLifeCycleIntervalException("The interval must be a positive number of seconds: ${interval.seconds}")
        }
        accountRepository.accountFor(userName)
            ?: throw AccountNotFoundException("Account not found for user: $userName")

        val rule = PasswordLifeCycleRule(
            userName = userName,
            interval = interval,
            creationDate = LocalDateTime.now(clock),
            lastEvaluationDate = null,
            action = action
        )
        try {
            rule.nextEvaluationDate()
        } catch (e: DateTimeException) {
            throw InvalidPasswordLifeCycleIntervalException("The interval is out of the supported date range: ${interval.seconds} seconds")
        }
        passwordLifeCycleRepository.store(rule)
    }

    fun rulesFor(userName: String): List<PasswordLifeCycleRule> =
        passwordLifeCycleRepository.findRulesFor(userName)

    fun remove(userName: String, action: PasswordLifeCycleAction) =
        passwordLifeCycleRepository.delete(userName, action)
}
