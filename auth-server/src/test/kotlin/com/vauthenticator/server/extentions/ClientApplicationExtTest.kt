package com.vauthenticator.server.extentions

import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.MFA_ALWAYS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.RESET_PASSWORD
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.SIGN_UP
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse

class ClientApplicationExtTest {
    val uut = aClientApp(aClientAppId()).copy(scopes = Scopes(setOf(ADMIN_FULL_ACCESS)))

    @Test
    fun `when ClientApplication scopes has full admin access`() {
        val uut = aClientApp(aClientAppId()).copy(scopes = Scopes(setOf(ADMIN_FULL_ACCESS)))
        assertTrue { uut.hasEnoughScopes(Scopes(setOf(MFA_ALWAYS))) }
    }

    @Test
    fun `when ClientApplication scopes check give a positive result without full access admin scope `() {
        val uut = aClientApp(aClientAppId()).copy(scopes = Scopes(setOf(RESET_PASSWORD, MFA_ALWAYS)))
        assertTrue { uut.hasEnoughScopes(Scopes(setOf(RESET_PASSWORD, MFA_ALWAYS))) }
    }

    @Test
    fun `when ClientApplication scopes check give a negative result because the use case require more scopes `() {
        val uut = aClientApp(aClientAppId()).copy(scopes = Scopes(setOf(RESET_PASSWORD)))
        assertFalse { uut.hasEnoughScopes(Scopes(setOf(RESET_PASSWORD, MFA_ALWAYS))) }
    }


    @Test
    fun `when a scope is under evaluation over ClientApplication scopes give a negative result`() {
        val uut = aClientApp(aClientAppId()).copy(scopes = Scopes(setOf(SIGN_UP)))
        assertFalse { uut.hasEnoughScopes(Scopes(setOf(MFA_ALWAYS))) }
    }


}