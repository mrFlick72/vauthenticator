package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.SAVE_CLIENT_APPLICATION
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.support.AdministrationFixture
import com.vauthenticator.server.support.AdministrationFixture.clientApplicationAdministrationFor
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ClientApplicationAdministrationTest {

    private val anAdministrator = clientApplicationAdministrationFor(AdministrationFixture.anAdministrator)
    private val notAnAdministrator = clientApplicationAdministrationFor(AdministrationFixture.notAnAdministrator)

    private val aClientApp = aClientApp(aClientAppId())
    private val anAdministratorClientApp = aClientApp(ClientAppId("admin")).copy(scopes = Scopes.from(ADMIN_FULL_ACCESS))

    @Test
    fun `a caller that is not an administrator saves a client app that is not an administrator`() {
        assertDoesNotThrow {
            notAnAdministrator.checkSave(aClientApp, aClientApp.copy(logoutUri = aClientApp.logoutUri.copy(content = "http://another_uri")))
        }
    }

    @Test
    fun `a caller that is not an administrator cannot give a client app the full access scope`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkSave(aClientApp, aClientApp.copy(scopes = Scopes(aClientApp.scopes.content + ADMIN_FULL_ACCESS)))
        }
    }

    @Test
    fun `a caller that is not an administrator cannot change an administrator client app even keeping its scopes`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkSave(anAdministratorClientApp, anAdministratorClientApp.copy(secret = Secret("a secret of my choice")))
        }
    }

    @Test
    fun `a caller that is not an administrator cannot take the full access scope away from a client app`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkSave(anAdministratorClientApp, anAdministratorClientApp.copy(scopes = Scopes.from(SAVE_CLIENT_APPLICATION)))
        }
    }

    @Test
    fun `an administrator creates a client app with the full access scope`() {
        assertDoesNotThrow { anAdministrator.checkSave(null, anAdministratorClientApp) }
    }

    @Test
    fun `a caller that is not an administrator cannot delete an administrator client app`() {
        assertThrows(AdministrationRequiredException::class.java) {
            notAnAdministrator.checkDelete(anAdministratorClientApp)
        }
    }

    @Test
    fun `a caller that is not an administrator deletes a client app that is not an administrator`() {
        assertDoesNotThrow { notAnAdministrator.checkDelete(aClientApp) }
    }

    @Test
    fun `deleting a client app that does not exist does not ask whether the caller is an administrator`() {
        assertDoesNotThrow { notAnAdministrator.checkDelete(null) }
    }
}
