package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.MFA_ALWAYS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.RESET_PASSWORD
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.SIGN_UP
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.support.SecurityFixture.principalFor
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class JwtAuthenticationTokenExtTest {

    @Test
    fun `when a jwt token scopes has full admin access`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", scopes = listOf(ADMIN_FULL_ACCESS.content))
        assertTrue { uut.hasEnoughScopes(Scopes(setOf(MFA_ALWAYS))) }
    }

    @Test
    fun `when jwt token  scopes check give a positive result without full access admin scope `() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", scopes = listOf(RESET_PASSWORD.content, MFA_ALWAYS.content))
        assertTrue { uut.hasEnoughScopes(Scopes(setOf(RESET_PASSWORD, MFA_ALWAYS))) }
    }

    @Test
    fun `when jwt token  scopes check give a negative result because the use case require more scopes `() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", scopes = listOf(RESET_PASSWORD.content))
        assertFalse { uut.hasEnoughScopes(Scopes(setOf(RESET_PASSWORD, MFA_ALWAYS))) }
    }


    @Test
    fun `when a scope is under evaluation over ClientApplication scopes give a negative result`() {
        val uut = m2mPrincipalFor("A_CLIENT_APP_ID", scopes = listOf(SIGN_UP.content))
        assertFalse { uut.hasEnoughScopes(Scopes(setOf(MFA_ALWAYS))) }
    }

    @Test
    fun `when the principal has the admin grant`() {
        val uut = principalFor(
            "A_CLIENT_APP_ID", scopes = listOf(SIGN_UP.content), email = "admin@email.com", authorities = listOf(
                Role.adminRole().name
            )
        )
        assertTrue { uut.hasEnoughScopes(Scopes(setOf(MFA_ALWAYS))) }
    }

}