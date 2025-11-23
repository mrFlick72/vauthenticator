package com.vauthenticator.server.account.domain

import java.security.Principal

class SaveAccount(private val accountRepository: AccountRepository) {
    fun execute(principal: Principal, account: Account) {
        return accountRepository.accountFor(principal.name)
            .ifPresent {
                val accountToBeSaved = account.copy(
                    email = principal.name,

                    firstName = account.firstName,
                    lastName = account.lastName,
                    birthDate = account.birthDate,
                    phone = account.phone,
                    locale = account.locale,

                    password = it.password,
                    authorities = it.authorities,
                    accountNonExpired = it.accountNonExpired,
                    accountNonLocked = it.accountNonLocked,
                    credentialsNonExpired = it.credentialsNonExpired,
                    enabled = it.enabled,
                    emailVerified = it.emailVerified,
                )
                accountRepository.save(accountToBeSaved)
            }
    }
}