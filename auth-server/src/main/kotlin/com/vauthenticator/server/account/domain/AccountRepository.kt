package com.vauthenticator.server.account.domain

interface AccountRepository {
    fun accountFor(username: String): Account?
    fun save(account: Account)
    fun create(account: Account)
}

class AccountRegistrationException(message: String, e: RuntimeException) : RuntimeException(message, e)
