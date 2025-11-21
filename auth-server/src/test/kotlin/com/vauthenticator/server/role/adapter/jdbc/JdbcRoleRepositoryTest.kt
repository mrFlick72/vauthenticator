package com.vauthenticator.server.role.adapter.jdbc

import com.vauthenticator.server.role.adapter.AbstractRoleRepositoryTest
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.JdbcUtils.initRoleTestsInDB
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.protectedRoleNames

class JdbcRoleRepositoryTest : AbstractRoleRepositoryTest() {
    override fun initRoleRepository(): RoleRepository =
        JdbcRoleRepository(jdbcTemplate, protectedRoleNames)

    override fun resetDatabase() {
        resetDb()
        initRoleTestsInDB()
    }

}