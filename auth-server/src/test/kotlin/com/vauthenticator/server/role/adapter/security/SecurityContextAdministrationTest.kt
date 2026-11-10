package com.vauthenticator.server.role.adapter.security

import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.SAVE_ACCOUNT
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.support.SecurityFixture.principalFor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class SecurityContextAdministrationTest {

    private val uut = SecurityContextAdministration()

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    private fun actingAs(caller: Authentication) {
        SecurityContextHolder.getContext().authentication = caller
    }

    @Test
    fun `an access token with the full access scope is an administrator`() {
        actingAs(m2mPrincipalFor("admin", scopes = listOf(ADMIN_FULL_ACCESS.content)))

        assertTrue(uut.isAdministrator())
    }

    @Test
    fun `an access token for an account with the administrator role is an administrator`() {
        actingAs(principalFor("vauthenticator-management-ui", "admin@email.com", listOf(Role.adminRole().name)))

        assertTrue(uut.isAdministrator())
    }

    @Test
    fun `an access token with neither the full access scope nor the administrator role is not an administrator`() {
        actingAs(m2mPrincipalFor("A_CLIENT_APP_ID", scopes = listOf(SAVE_ACCOUNT.content)))

        assertFalse(uut.isAdministrator())
    }

    @Test
    fun `a browser session is never an administrator even when its account has the administrator role`() {
        actingAs(principalFor("admin@email.com", listOf(Role.adminRole().name)))

        assertFalse(uut.isAdministrator())
    }

    @Test
    fun `no caller is not an administrator`() {
        assertFalse(uut.isAdministrator())
    }

    @Test
    fun `requiring an administrator passes for an administrator`() {
        actingAs(m2mPrincipalFor("admin", scopes = listOf(ADMIN_FULL_ACCESS.content)))

        assertDoesNotThrow { uut.requireAdministrator() }
    }

    @Test
    fun `requiring an administrator fails for a caller that is not an administrator`() {
        actingAs(m2mPrincipalFor("A_CLIENT_APP_ID", scopes = listOf(SAVE_ACCOUNT.content)))

        assertThrows(AdministrationRequiredException::class.java) { uut.requireAdministrator() }
    }
}
