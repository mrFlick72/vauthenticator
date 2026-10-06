package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalDateTime


class PasswordLifeCycleExecutor(
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository,
    private val strategies: List<PasswordLifeCycleStrategy>
) {
    fun register(rule: PasswordLifeCycleRule) {
        passwordLifeCycleRepository.store(rule)
    }

    fun execute(rule: PasswordLifeCycleRule) {
        val strategy = strategies.find { it.canHandle(rule) }
        strategy?.execute(rule) ?: throw NoopPasswordLifeCycleStrategyException(rule)
    }
}

interface PasswordLifeCycleStrategy {
    fun execute(rule: PasswordLifeCycleRule)
    fun canHandle(rule: PasswordLifeCycleRule): Boolean
}

class NoopPasswordLifeCycleStrategyException(rule: PasswordLifeCycleRule) :
    RuntimeException("No strategy found for rule: $rule")

interface PasswordLifeCycleStrategyImplementation {
    fun execute(account: Account)
    fun canHandle(rule: PasswordLifeCycleRule): Boolean
}

class BasePasswordLifeCycleStrategy(
    private val clock: Clock,
    private val accountRepository: AccountRepository,
    private val implementation: PasswordLifeCycleStrategyImplementation
) : PasswordLifeCycleStrategy {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun execute(rule: PasswordLifeCycleRule) {
        accountRepository.accountFor(rule.userName)?.let { account ->

            // last evaluation date is null so we need to evaluate if the ttl has expired upon the creation date of the rule
            val lastEvaluationDate = rule.lastEvaluationDate ?: rule.creationDate
            val expirationDate = lastEvaluationDate.plus(rule.ttl)
            if (LocalDateTime.now(clock).isAfter(expirationDate)) {
                implementation.execute(account)
            } else {
                logger.info("Password reset rule expired for user: ${rule.userName}")
            }

        } ?: throw AccountNotFoundException("Account not found for user: ${rule.userName}")
    }

    override fun canHandle(rule: PasswordLifeCycleRule): Boolean {
        return implementation.canHandle(rule)
    }
}

class PasswordResetPasswordLifeCycleStrategyImplementation(
    private val accountRepository: AccountRepository,

    ) : PasswordLifeCycleStrategyImplementation {
    override fun execute(account: Account) {
        accountRepository.save(account.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD))
    }

    override fun canHandle(rule: PasswordLifeCycleRule): Boolean {
        return rule.action == PasswordLifeCycleAction.PASSWORD_RESET
    }
}

class AccountLockPasswordLifeCycleStrategyImplementation
    (
    private val accountRepository: AccountRepository
) : PasswordLifeCycleStrategyImplementation {

    override fun execute(account: Account) {
        accountRepository.save(
            account.copy(accountNonLocked = true)
        )
    }

    override fun canHandle(rule: PasswordLifeCycleRule): Boolean {
        return rule.action == PasswordLifeCycleAction.ACCOUNT_LOCK
    }
}