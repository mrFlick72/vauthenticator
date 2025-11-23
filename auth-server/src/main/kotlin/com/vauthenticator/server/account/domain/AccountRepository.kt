package com.vauthenticator.server.account.domain

import java.util.*

interface AccountRepository {
    fun accountFor(username: String): Optional<Account>
    fun save(account: Account)
    fun create(account: Account)
}

class AccountRegistrationException(message: String, e: RuntimeException) : RuntimeException(message, e)