package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.role.domain.Administration

/**
 * Only an Administrator writes a client app that has, or would get, the Full-Access Scope (ADR 0017). Any write
 * counts, not only a scope change: a new secret, grant type or redirect URI takes an Administrator client app over
 * as surely as granting the scope.
 * Every writer of client apps calls it before writing; `AdministrationArchitectureTest` fails the build otherwise.
 */
class ClientApplicationAdministration(private val administration: Administration) {

    fun checkSave(stored: ClientApplication?, requested: ClientApplication) {
        if (isAdministrator(stored) || isAdministrator(requested)) {
            administration.requireAdministrator()
        }
    }

    fun checkDelete(stored: ClientApplication?) {
        if (isAdministrator(stored)) {
            administration.requireAdministrator()
        }
    }

    private fun isAdministrator(clientApp: ClientApplication?) =
        clientApp?.scopes?.content.orEmpty().contains(Scope.ADMIN_FULL_ACCESS)
}
