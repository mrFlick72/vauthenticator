package com.vauthenticator.server.password.domain.lifecycle

interface PasswordLifeCycleRepository {

    fun store(rule: PasswordLifeCycleRule)

    fun retrieve(userName: String): PasswordLifeCycleRule?

    fun delete(userName: String)

    fun findAllRules(page: Int, size: Int): List<PasswordLifeCycleRule>

}