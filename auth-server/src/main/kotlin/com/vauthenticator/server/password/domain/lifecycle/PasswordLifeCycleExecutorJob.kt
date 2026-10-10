package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import org.slf4j.LoggerFactory


private const val SIZE = 100

class PasswordLifeCycleExecutorJob(
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository,
    private val passwordLifeCycleExecutor: PasswordLifeCycleExecutor,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute() {
        var rules = passwordLifeCycleRepository.findAllRulesAfter(after = null, size = SIZE)
        while (rules.isNotEmpty()) {
            rules.forEach { rule -> executeIsolated(rule) }
            rules = passwordLifeCycleRepository.findAllRulesAfter(after = rules.last(), size = SIZE)
        }
    }

    private fun executeIsolated(rule: PasswordLifeCycleRule) {
        try {
            try {
                passwordLifeCycleExecutor.execute(rule)
            } catch (e: AccountNotFoundException) {
                logger.warn("Password lifecycle rule ${rule.action} for user ${rule.userName} is removed: ${e.message}")
                passwordLifeCycleRepository.delete(rule.userName, rule.action)
            }
        } catch (e: RuntimeException) {
            logger.error("Password lifecycle rule ${rule.action} for user ${rule.userName} failed", e)
        }
    }
}
