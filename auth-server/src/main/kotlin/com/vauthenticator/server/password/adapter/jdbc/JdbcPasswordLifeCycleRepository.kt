package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRepository
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule


//todo TBD
class JdbcPasswordLifeCycleRepository : PasswordLifeCycleRepository {
    override fun store(rule: PasswordLifeCycleRule) {
        TODO("Not yet implemented")
    }

    override fun retrieve(userName: String): PasswordLifeCycleRule? {
        TODO("Not yet implemented")
    }

    override fun delete(userName: String) {
        TODO("Not yet implemented")
    }

    override fun findAllRules(
        page: Int,
        size: Int
    ): List<PasswordLifeCycleRule> {
        TODO("Not yet implemented")
    }
}