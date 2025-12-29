package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.MFA_ALWAYS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.RESET_PASSWORD
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.SIGN_UP
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class JwtAuthenticationTokenExtTest {
    @Test
    fun `when full admin access scopes under evaluation over ClientApplication scopes`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", listOf(SIGN_UP.content))

        assertTrue { uut.hasEnoughScopes(Scopes(setOf(ADMIN_FULL_ACCESS, MFA_ALWAYS))) }
        assertTrue { uut.hasEnoughScopes((ADMIN_FULL_ACCESS)) }
    }

    @Test
    fun `when several scopes are under evaluation over ClientApplication scopes give a positive result`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", listOf(RESET_PASSWORD.content))

        assertTrue { uut.hasEnoughScopes(Scopes(setOf(RESET_PASSWORD, MFA_ALWAYS))) }
    }


    @Test
    fun `when a scope is under evaluation over ClientApplication scopes give a positive result`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", listOf(RESET_PASSWORD.content))

        assertTrue { uut.hasEnoughScopes(RESET_PASSWORD) }
    }


    @Test
    fun `when several scopes are under evaluation over ClientApplication scopes give a negative result`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", listOf(SIGN_UP.content))

        assertFalse { uut.hasEnoughScopes(MFA_ALWAYS) }
    }


    @Test
    fun `when a scope is under evaluation over ClientApplication scopes give a negative result`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", listOf(RESET_PASSWORD.content))

        assertFalse { uut.hasEnoughScopes(Scopes(setOf(SIGN_UP, MFA_ALWAYS))) }
    }
}