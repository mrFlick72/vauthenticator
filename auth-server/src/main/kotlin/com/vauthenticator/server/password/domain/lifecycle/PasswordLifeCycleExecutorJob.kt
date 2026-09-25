package com.vauthenticator.server.password.domain.lifecycle


private const val SIZE = 100

class PasswordLifeCycleExecutorJob(
    private val passwordLifeCycleRepository: PasswordLifeCycleRepository,
    private val passwordLifeCycleExecutor: PasswordLifeCycleExecutor,
) {

    fun execute() {
        var index = 0;
        var rules = passwordLifeCycleRepository.findAllRules(page = index, size = SIZE)
        while (rules.isNotEmpty()) {
            rules.forEach { rule ->
                passwordLifeCycleExecutor.execute(rule)
            }

            index++
            rules = passwordLifeCycleRepository.findAllRules(page = index, size = SIZE)
        }

    }
}

