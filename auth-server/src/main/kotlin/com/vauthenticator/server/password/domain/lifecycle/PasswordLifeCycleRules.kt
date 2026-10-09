package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountPattern
import com.vauthenticator.server.account.domain.AccountRepository
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime

private const val PAGE_SIZE = 100

class PasswordLifeCycleRules(
    private val clock: Clock,
    private val accountRepository: AccountRepository,
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository
) {

    fun register(userName: String, action: PasswordLifeCycleAction, interval: Duration) {
        val now = LocalDateTime.now(clock)
        validate(interval, now)
        accountRepository.accountFor(userName)
            ?: throw AccountNotFoundException("Account not found for user: $userName")

        passwordLifeCycleRepository.store(newRule(userName, action, interval, now))
    }

    /**
     * Registers the rule for every account matching [pattern] at this moment, replacing any rule the account already
     * has for [action], and returns how many accounts matched.
     */
    fun registerAll(pattern: AccountPattern, action: PasswordLifeCycleAction, interval: Duration): Int {
        val now = LocalDateTime.now(clock)
        validate(interval, now)

        var matched = 0
        var userNames = accountRepository.findUserNamesMatching(pattern, after = null, size = PAGE_SIZE)
        while (userNames.isNotEmpty()) {
            userNames.forEach { passwordLifeCycleRepository.store(newRule(it, action, interval, now)) }
            matched += userNames.size
            userNames = accountRepository.findUserNamesMatching(pattern, after = userNames.last(), size = PAGE_SIZE)
        }
        return matched
    }

    fun rulesFor(userName: String): List<PasswordLifeCycleRule> =
        passwordLifeCycleRepository.findRulesFor(userName)

    fun remove(userName: String, action: PasswordLifeCycleAction) =
        passwordLifeCycleRepository.delete(userName, action)

    fun removeAll(pattern: AccountPattern, action: PasswordLifeCycleAction): Int =
        passwordLifeCycleRepository.deleteMatching(pattern, action)

    private fun validate(interval: Duration, now: LocalDateTime) {
        if (interval.isZero || interval.isNegative) {
            throw InvalidPasswordLifeCycleIntervalException("The interval must be a positive number of seconds: ${interval.seconds}")
        }
        if (!this.runCatching { now.plus(interval) }.isSuccess) {
            throw InvalidPasswordLifeCycleIntervalException("The interval is out of the supported date range: ${interval.seconds} seconds")
        }
    }

    private fun newRule(userName: String, action: PasswordLifeCycleAction, interval: Duration, now: LocalDateTime) =
        PasswordLifeCycleRule(
            userName = userName,
            interval = interval,
            creationDate = now,
            lastEvaluationDate = null,
            action = action
        )
}
