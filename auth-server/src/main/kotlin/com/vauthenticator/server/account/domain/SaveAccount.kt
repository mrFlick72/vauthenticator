package com.vauthenticator.server.account.domain

import java.security.Principal

class SaveAccount(private val accountRepository: AccountRepository) {
    fun execute(principal: Principal, account: Account) {
        ownershipCheck(principal, account)
        accountRepository.accountFor(principal.name)?.let {
            val accountToBeSaved = it.copy(
                firstName = account.firstName,
                lastName = account.lastName,
                birthDate = account.birthDate,
                phone = account.phone,
                locale = account.locale,
            )
            accountRepository.save(accountToBeSaved)
        }
    }

    private fun ownershipCheck(principal: Principal, account: Account) {
        if (account.email.isNotEmpty() && account.email != principal.name) {
            throw AccountOwnershipException("the account to update does not belong to the authenticated user")
        }
    }
}
