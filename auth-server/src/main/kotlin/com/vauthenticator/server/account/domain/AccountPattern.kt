package com.vauthenticator.server.account.domain

/**
 * A case-insensitive username pattern: `*` matches any run of characters, every other character is literal,
 * and `*` alone selects every account.
 */
data class AccountPattern(val value: String) {
    init {
        if (value.isBlank()) {
            throw InvalidAccountPatternException("The account pattern must not be blank")
        }
    }
}

class InvalidAccountPatternException(message: String) : RuntimeException(message)

class AccountPatternSearchNotSupportedException(message: String) : RuntimeException(message)
