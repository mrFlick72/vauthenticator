package com.vauthenticator.server.account.adapter.jdbc

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.adapter.AbstractAccountRepositoryTest
import com.vauthenticator.server.role.adapter.jdbc.JdbcRoleRepository
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.protectedRoleNames

class JdbcAccountRepositoryTest : AbstractAccountRepositoryTest() {

    override fun initUnitUnderTest(roleRepository: RoleRepository): AccountRepository =
        JdbcAccountRepository(jdbcTemplate)

    override fun initRoleRepository(): RoleRepository = JdbcRoleRepository(jdbcTemplate, protectedRoleNames)

    override fun resetDatabase() {
        resetDb()
    }

}