package com.vauthenticator.server.password.domain.lifecycle

import java.time.LocalDateTime

interface PasswordLifeCycleRepository {

    fun store(rule: PasswordLifeCycleRule)

    fun delete(userName: String)

    fun updateLastEvaluationDate(rule: PasswordLifeCycleRule, lastEvaluationDate: LocalDateTime)

    fun findAllRules(page: Int, size: Int): List<PasswordLifeCycleRule>

}