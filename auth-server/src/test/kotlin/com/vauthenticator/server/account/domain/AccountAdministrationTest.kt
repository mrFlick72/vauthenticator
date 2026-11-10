package com.vauthenticator.server.account.domain

import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.support.AdministrationFixture
import com.vauthenticator.server.support.AdministrationFixture.accountAdministrationFor
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AccountAdministrationTest {

    private val anAdministrator = accountAdministrationFor(AdministrationFixture.anAdministrator)
    private val notAnAdministrator = accountAdministrationFor(AdministrationFixture.notAnAdministrator)

    private val anAdministratorAccount = anAccount().copy(authorities = setOf(Role.adminRole().name))

    @Test
    fun `a caller that is not an administrator saves an account without changing its administration`() {
        val stored = anAccount()

        assertDoesNotThrow { notAnAdministrator.checkSave(stored, stored.copy(enabled = false)) }
    }

    @Test
    fun `a caller that is not an administrator keeps an administrator account an administrator`() {
        assertDoesNotThrow { notAnAdministrator.checkSave(anAdministratorAccount, anAdministratorAccount.lock()) }
    }

    @Test
    fun `a caller that is not an administrator cannot give an account the administrator role`() {
        val stored = anAccount()

        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkSave(stored, stored.copy(authorities = setOf(Role.adminRole().name)))
        }
    }

    @Test
    fun `a caller that is not an administrator cannot take the administrator role away from an account`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkSave(anAdministratorAccount, anAdministratorAccount.copy(authorities = emptySet()))
        }
    }

    @Test
    fun `an administrator gives an account the administrator role`() {
        val stored = anAccount()

        assertDoesNotThrow { anAdministrator.checkSave(stored, stored.copy(authorities = setOf(Role.adminRole().name))) }
    }

    @Test
    fun `an administrator takes the administrator role away from an account`() {
        assertDoesNotThrow {
            anAdministrator.checkSave(anAdministratorAccount, anAdministratorAccount.copy(authorities = emptySet()))
        }
    }

    @Test
    fun `a caller that is not an administrator cannot create an account with the administrator role`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkCreate(anAdministratorAccount)
        }
    }

    @Test
    fun `a caller that is not an administrator creates an account without the administrator role`() {
        assertDoesNotThrow { notAnAdministrator.checkCreate(anAccount().copy(authorities = setOf(Role.defaultRole().name))) }
    }

    @Test
    fun `an administrator creates an account with the administrator role`() {
        assertDoesNotThrow { anAdministrator.checkCreate(anAdministratorAccount) }
    }

    @Test
    fun `a caller that is not an administrator cannot manage an administrator account`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkManage(anAdministratorAccount)
        }
    }

    @Test
    fun `a caller that is not an administrator manages an account that is not an administrator`() {
        assertDoesNotThrow { notAnAdministrator.checkManage(anAccount()) }
    }

    @Test
    fun `an administrator manages an administrator account`() {
        assertDoesNotThrow { anAdministrator.checkManage(anAdministratorAccount) }
    }

    @Test
    fun `only an administrator manages every account`() {
        assertTrue(anAdministrator.managesEveryAccount())
        assertFalse(notAnAdministrator.managesEveryAccount())
    }

    @Test
    fun `a caller that is not an administrator can manage only accounts that are not administrators`() {
        assertTrue(notAnAdministrator.canManage(anAccount()))
        assertFalse(notAnAdministrator.canManage(anAdministratorAccount))
        assertTrue(anAdministrator.canManage(anAdministratorAccount))
    }
}
