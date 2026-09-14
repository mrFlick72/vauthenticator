package com.vauthenticator.server.password.domain.lifecycle


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
    RuntimeException("No strategy found for rule: $rule") {
}