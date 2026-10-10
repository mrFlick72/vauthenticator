package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.PasswordLifeCycleActionAppliedEvent
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalDateTime


class PasswordLifeCycleExecutor(
    private val strategies: List<PasswordLifeCycleStrategy>
) {
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
    /**
     * Returns the account with the action applied, or null when the action is already in effect.
     */
    fun apply(account: Account): Account?
    fun canHandle(rule: PasswordLifeCycleRule): Boolean
}

class BasePasswordLifeCycleStrategy(
    private val clock: Clock,
    private val accountRepository: AccountRepository,
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository,
    private val eventsDispatcher: EventsDispatcher,
    private val implementation: PasswordLifeCycleStrategyImplementation
) : PasswordLifeCycleStrategy {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun execute(rule: PasswordLifeCycleRule) {
        val account = accountRepository.accountFor(rule.userName)
            ?: throw AccountNotFoundException("Account not found for user: ${rule.userName}")

        val now = LocalDateTime.now(clock)
        if (!rule.isDue(now)) {
            logger.info("Password lifecycle rule ${rule.action} not expired yet for user: ${rule.userName}")
            return
        }

        implementation.apply(account)?.let { updatedAccount ->
            accountRepository.save(updatedAccount)
            eventsDispatcher.dispatch(
                PasswordLifeCycleActionAppliedEvent(
                    Email(rule.userName),
                    ClientAppId.system(),
                    clock.instant(),
                    rule.action
                )
            )
        }

        if (rule.action.oneShot) {
            passwordLifeCycleRepository.delete(rule.userName, rule.action)
        } else {
            passwordLifeCycleRepository.updateLastEvaluationDate(rule, now)
        }
    }

    override fun canHandle(rule: PasswordLifeCycleRule): Boolean {
        return implementation.canHandle(rule)
    }
}

class PasswordResetPasswordLifeCycleStrategyImplementation : PasswordLifeCycleStrategyImplementation {
    override fun apply(account: Account): Account? =
        if (account.mandatoryAction == AccountMandatoryAction.RESET_PASSWORD) {
            null
        } else {
            account.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD)
        }

    override fun canHandle(rule: PasswordLifeCycleRule): Boolean {
        return rule.action == PasswordLifeCycleAction.PASSWORD_RESET
    }
}

class AccountLockPasswordLifeCycleStrategyImplementation : PasswordLifeCycleStrategyImplementation {
    override fun apply(account: Account): Account? =
        if (account.accountNonLocked) {
            account.copy(accountNonLocked = false)
        } else {
            null
        }

    override fun canHandle(rule: PasswordLifeCycleRule): Boolean {
        return rule.action == PasswordLifeCycleAction.ACCOUNT_LOCK
    }
}
