package com.vauthenticator.server.support

import com.vauthenticator.server.account.domain.*
import com.vauthenticator.server.role.domain.Role

const val EMAIL = "email@domain.com"

object AccountTestFixture {

    fun anAccount() = Account(
        enabled = true,
        username = EMAIL,
        password = "secret",
        authorities = emptySet(),
        groups = emptySet(),
        email = EMAIL,
        firstName = "A First Name",
        lastName = "A Last Name",
        birthDate = Date.empty(),
        phone = Phone.empty(),
        locale = UserLocale.empty(),
        mandatoryAction = AccountMandatoryAction.NO_ACTION
    )

    fun anAccountWithPhoneNumber() = Account(
        enabled = true,
        username = EMAIL,
        password = "secret",
        authorities = emptySet(),
        groups = emptySet(),
        email = EMAIL,
        firstName = "A First Name",
        lastName = "A Last Name",
        birthDate = Date.empty(),
        phone = Phone.phoneFor("+39 339 2323223"),
        locale = UserLocale.empty(),
        mandatoryAction = AccountMandatoryAction.NO_ACTION
    )

    fun anAccount(roles: Set<Role>) = anAccount().copy(authorities = roles.map { it.name }.toSet())
}