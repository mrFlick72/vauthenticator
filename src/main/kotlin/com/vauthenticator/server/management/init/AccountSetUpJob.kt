package com.vauthenticator.server.management.init

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import java.util.*

class AccountSetUpJob(
    private val roleRepository: RoleRepository,
    private val accountRepository: AccountRepository,
    private val passwordEncoder: VAuthenticatorPasswordEncoder
) {
    fun execute() {
        val userRole = Role("ROLE_USER", "Generic user role")
        val adminRole = Role("VAUTHENTICATOR_ADMIN", "VAuthenticator admin role")

        roleRepository.save(userRole)
        roleRepository.save(adminRole)

        accountRepository.save(
            Account(
                accountNonExpired = true,
                accountNonLocked = true,
                credentialsNonExpired = true,
                enabled = true,
                "admin@email.com",
                passwordEncoder.encode("secret"),
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
        )
    }

}