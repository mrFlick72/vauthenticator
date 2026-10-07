package com.vauthenticator.server.account.adapter.jdbc

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.adapter.AbstractAccountRepositoryTest
import com.vauthenticator.server.role.adapter.jdbc.JdbcRoleRepository
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.protectedRoleNames
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JdbcAccountRepositoryTest : AbstractAccountRepositoryTest() {

    override fun initUnitUnderTest(roleRepository: RoleRepository): AccountRepository =
        JdbcAccountRepository(jdbcTemplate)

    override fun initRoleRepository(): RoleRepository = JdbcRoleRepository(jdbcTemplate, protectedRoleNames)

    override fun resetDatabase() {
        resetDb()
    }

    @Test
    fun `when an account is saved the groups of the other accounts are preserved`() {
        jdbcTemplate.update("INSERT INTO GROUPS (name, description) VALUES (?, ?)", "a_group", "description")
        val uut = JdbcAccountRepository(jdbcTemplate)
        val account = anAccount().copy(groups = setOf("a_group"))
        val anotherAccount = account.copy(username = "another@email.com", email = "another@email.com")
        uut.save(account)
        uut.save(anotherAccount)

        uut.save(account.copy(firstName = "A_NEW_FIRSTNAME"))

        assertEquals(anotherAccount, uut.accountFor(anotherAccount.username))
    }

}
