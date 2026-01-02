package com.vauthenticator.server.management.init

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.signup.SignUpUse
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import java.util.*

class AccountSetUpJob(
    private val roleRepository: RoleRepository,
    private val signUpUse: SignUpUse,
) {
    fun execute() {
        val userRole = Role("ROLE_USER", "Generic user role")
        val adminRole = Role("VAUTHENTICATOR_ADMIN", "VAuthenticator admin role")
        val account = Account(
            accountNonExpired = true,
            accountNonLocked = true,
            credentialsNonExpired = true,
            enabled = true,
            "admin@email.com",
            "secret!",
            authorities = setOf(userRole.name, adminRole.name),
            groups = emptySet(),
            email = "admin@email.com",
            emailVerified = true,
            firstName = "Admin",
            lastName = "",
            birthDate = Optional.empty(),
            phone = Optional.empty(),
            locale = Optional.empty(),
            mandatoryAction = AccountMandatoryAction.NO_ACTION
        )

        roleRepository.save(userRole)
        roleRepository.save(adminRole)

        signUpUse.execute(ClientAppId("admin"), account)
    }

}