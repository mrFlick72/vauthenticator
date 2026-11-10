package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountAdministration
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
    private val accountAdministration: AccountAdministration,
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository
) {

    fun register(userName: String, action: PasswordLifeCycleAction, interval: Duration) {
        val now = LocalDateTime.now(clock)
        validate(interval, now)
        val account = accountRepository.accountFor(userName)
            ?: throw AccountNotFoundException("Account not found for user: $userName")
        accountAdministration.checkManage(account)

        passwordLifeCycleRepository.store(newRule(userName, action, interval, now))
    }

    /**
     * Registers the rule for every account matching [pattern] at this moment, replacing any rule the account already
     * has for [action]. Accounts the caller cannot manage (an Administrator account, when the caller is not an
     * Administrator) are skipped, not failed (ADR 0018).
     */
    fun registerAll(pattern: AccountPattern, action: PasswordLifeCycleAction, interval: Duration): PasswordLifeCycleBulkOutcome {
        val now = LocalDateTime.now(clock)
        validate(interval, now)
        return forEachManageableAccount(pattern) { passwordLifeCycleRepository.store(newRule(it, action, interval, now)); 1 }
    }

    fun rulesFor(userName: String): List<PasswordLifeCycleRule> =
        passwordLifeCycleRepository.findRulesFor(userName)

    fun remove(userName: String, action: PasswordLifeCycleAction) {
        accountRepository.accountFor(userName)?.let { accountAdministration.checkManage(it) }
        passwordLifeCycleRepository.delete(userName, action)
    }

    /**
     * Removes the rule for [action] from every account matching [pattern], skipping the accounts the caller cannot
     * manage like [registerAll]; `applied` counts the rules removed.
     */
    fun removeAll(pattern: AccountPattern, action: PasswordLifeCycleAction): PasswordLifeCycleBulkOutcome =
        forEachManageableAccount(pattern) { passwordLifeCycleRepository.delete(it, action) }

    private fun forEachManageableAccount(pattern: AccountPattern, apply: (String) -> Int): PasswordLifeCycleBulkOutcome {
        val managesEveryAccount = accountAdministration.managesEveryAccount()
        var applied = 0
        var skipped = 0
        var userNames = accountRepository.findUserNamesMatching(pattern, after = null, size = PAGE_SIZE)
        while (userNames.isNotEmpty()) {
            userNames.forEach { userName ->
                if (managesEveryAccount || canManage(userName)) applied += apply(userName) else skipped++
            }
            userNames = accountRepository.findUserNamesMatching(pattern, after = userNames.last(), size = PAGE_SIZE)
        }
        return PasswordLifeCycleBulkOutcome(applied, skipped)
    }

    private fun canManage(userName: String) =
        accountRepository.accountFor(userName)?.let { accountAdministration.canManage(it) } ?: true

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

/**
 * [applied]: the accounts the bulk operation was applied to; [skipped]: the matching accounts the caller cannot manage.
 */
data class PasswordLifeCycleBulkOutcome(val applied: Int, val skipped: Int)
