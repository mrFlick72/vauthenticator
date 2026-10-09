package com.vauthenticator.server.account.adapter.jdbc

import com.vauthenticator.server.account.domain.AccountPattern
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

    @Test
    fun `usernames matching an account pattern are found`() {
        val userNames = listOf(
            "alice@gmail.com", "Bob@Gmail.COM", "carol@example.com", "a_b@example.com", "axb@example.com", "a%b@example.com"
        )
        val uut = JdbcAccountRepository(jdbcTemplate)
        userNames.forEach { uut.create(anAccount().copy(username = it, email = it)) }
        fun allMatching(pattern: String) = uut.findUserNamesMatching(AccountPattern(pattern), after = null, size = 100)

        assertEquals(userNames.sorted(), allMatching("*"))
        assertEquals(listOf("Bob@Gmail.COM", "alice@gmail.com"), allMatching("*@gmail.com"))
        assertEquals(listOf("carol@example.com"), allMatching("carol@example.com"))
        assertEquals(listOf("a_b@example.com"), allMatching("a_b@*"))
        assertEquals(listOf("a%b@example.com"), allMatching("a%b@*"))
        assertEquals(emptyList<String>(), allMatching("*@gmial.com"))
    }

    @Test
    fun `usernames matching an account pattern are read with keyset pagination`() {
        val userNames = listOf("a@x.com", "b@x.com", "c@x.com", "d@x.com", "e@x.com")
        val uut = JdbcAccountRepository(jdbcTemplate)
        userNames.forEach { uut.create(anAccount().copy(username = it, email = it)) }

        val firstPage = uut.findUserNamesMatching(AccountPattern("*"), after = null, size = 3)
        val secondPage = uut.findUserNamesMatching(AccountPattern("*"), after = firstPage.last(), size = 3)
        val thirdPage = uut.findUserNamesMatching(AccountPattern("*"), after = secondPage.last(), size = 3)

        assertEquals(userNames, firstPage + secondPage)
        assertEquals(emptyList<String>(), thirdPage)
    }

}
