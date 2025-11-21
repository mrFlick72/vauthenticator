package com.vauthenticator.server.account.adapter

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction.RESET_PASSWORD
import com.vauthenticator.server.account.domain.AccountRegistrationException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.defaultRole
import com.vauthenticator.server.support.protectedRoleName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

abstract class AbstractAccountRepositoryTest {

    private val role = Role("role", "description")
    private val anotherRole = Role("another_role", "description")
    private val account = anAccount(setOf(role, defaultRole))

    private lateinit var uut: AccountRepository
    private lateinit var roleRepository: RoleRepository

    abstract fun initUnitUnderTest(roleRepository: RoleRepository): AccountRepository
    abstract fun initRoleRepository(): RoleRepository
    abstract fun resetDatabase()

    @BeforeEach
    fun setUp() {
        resetDatabase()

        roleRepository = initRoleRepository()
        uut = initUnitUnderTest(roleRepository)


        roleRepository.save(Role(protectedRoleName, protectedRoleName))
        roleRepository.save(role)
        roleRepository.save(anotherRole)
    }

    @Test
    fun `find an account by email`() {
        uut.save(account)
        val findByUsername: Account = uut.accountFor(account.username).orElseThrow()

        assertEquals(account, findByUsername)
    }

    @Test
    fun `find an account by email with reset password as mandatory action`() {
        val account = account.copy(mandatoryAction = RESET_PASSWORD)
        uut.save(account)
        val actual: Account = uut.accountFor(account.username).orElseThrow()

        assertEquals(actual, account)
    }

    @Test
    fun `find an account by email after one master role delete`() {
        uut.save(account)
        roleRepository.delete(role.name)

        val findByUsername: Account = uut.accountFor(account.username).orElseThrow()

        assertEquals(findByUsername, account.copy(authorities = setOf(protectedRoleName)))
    }

    @Test
    fun `find an account by email when an account does not exist`() {
        uut.save(account)
        val findByUsername: Optional<Account> = uut.accountFor("not-existing-user-name")

        val empty: Optional<Account> = Optional.empty()
        assertEquals(findByUsername, empty)
    }

    @Test
    fun `save an account by email`() {
        uut.save(account)

        val findByUsername: Account = uut.accountFor(account.username).orElseThrow()
        assertEquals(findByUsername, account)

        val accountUpdated = account.copy(firstName = "A_NEW_FIRSTNAME", lastName = "A_NEW_LASTNAME")
        uut.save(accountUpdated)

        val updatedFindByUsername = uut.accountFor(account.username).orElseThrow()
        assertEquals(updatedFindByUsername, accountUpdated)
    }


    @Test
    fun `when overrides authorities to an accounts`() {
        val anotherAccount = account.copy(
            authorities = setOf("another_role")
        )
        uut.save(account)
        uut.save(anotherAccount)

        assertEquals(uut.accountFor(account.username), Optional.of(anotherAccount))
    }

    @Test
    internal fun `when a new account is created`() {
        uut.create(account)

        assertEquals(uut.accountFor(account.username), Optional.of(account))
    }

    @Test
    internal fun `when a new account is created then once`() {
        assertThrows(AccountRegistrationException::class.java) {
            uut.create(account)
            uut.create(account)
        }
    }
}