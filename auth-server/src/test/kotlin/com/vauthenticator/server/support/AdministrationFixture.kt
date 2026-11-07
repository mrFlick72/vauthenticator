package com.vauthenticator.server.support

import com.vauthenticator.server.account.domain.AccountAdministration
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationAdministration
import com.vauthenticator.server.role.domain.Administration
import com.vauthenticator.server.role.domain.AdministrationRequiredException

object AdministrationFixture {

    val anAdministrator: Administration = object : Administration {
        override fun requireAdministrator() {}
    }

    val notAnAdministrator: Administration = object : Administration {
        override fun requireAdministrator() = throw AdministrationRequiredException("not an administrator")
    }

    fun accountAdministrationFor(caller: Administration) = AccountAdministration(caller)

    fun clientApplicationAdministrationFor(caller: Administration) = ClientApplicationAdministration(caller)
}
