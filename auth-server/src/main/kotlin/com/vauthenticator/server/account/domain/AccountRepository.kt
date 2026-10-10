package com.vauthenticator.server.account.domain

interface AccountRepository {
    fun accountFor(username: String): Account?
    fun save(account: Account)
    fun create(account: Account)

    /**
     * Keyset pagination ordered by username: returns up to [size] usernames matching [pattern] that come strictly
     * after [after], or the first page when [after] is null.
     */
    fun findUserNamesMatching(pattern: AccountPattern, after: String?, size: Int): List<String>
}

class AccountRegistrationException(message: String, e: RuntimeException) : RuntimeException(message, e)
