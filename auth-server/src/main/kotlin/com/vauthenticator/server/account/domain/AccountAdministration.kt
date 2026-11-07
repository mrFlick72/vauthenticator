package com.vauthenticator.server.account.domain

import com.vauthenticator.server.role.domain.Administration
import com.vauthenticator.server.role.domain.Role

/**
 * Only an Administrator gives an account the Administrator Role, or takes it away (ADR 0017).
 * Every writer of accounts calls it before writing; `AdministrationArchitectureTest` fails the build otherwise.
 */
class AccountAdministration(private val administration: Administration) {

    fun checkSave(stored: Account?, requested: Account) {
        if (isAdministrator(stored) != isAdministrator(requested)) {
            administration.requireAdministrator()
        }
    }

    // only the new account counts: comparing it with a stored one would tell a signup whether its username
    // belongs to an Administrator, and creating over an existing account fails anyway
    fun checkCreate(requested: Account) {
        if (isAdministrator(requested)) {
            administration.requireAdministrator()
        }
    }

    private fun isAdministrator(account: Account?) =
        account?.authorities.orEmpty().contains(Role.adminRole().name)
}
