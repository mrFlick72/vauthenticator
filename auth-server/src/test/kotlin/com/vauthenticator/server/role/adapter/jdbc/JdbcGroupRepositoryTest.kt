package com.vauthenticator.server.role.adapter.jdbc

import com.vauthenticator.server.role.adapter.AbstractGroupRepositoryTest
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.JdbcUtils.initRoleTestsInDB
import com.vauthenticator.server.support.JdbcUtils.jdbcClient
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.protectedRoleNames


class JdbcGroupRepositoryTest : AbstractGroupRepositoryTest() {

    override fun initGroupRepository(): GroupRepository =
        JdbcGroupRepository(jdbcClient)

    override fun initRoleRepository(): RoleRepository =
        JdbcRoleRepository(jdbcTemplate, protectedRoleNames)


    override fun resetDatabase() {
        resetDb()
        initRoleTestsInDB()

        roleRepository.save(Role("a_role_name", "a_role_description"))
        roleRepository.save(Role("another_role_name", "another_role_description"))
    }

}