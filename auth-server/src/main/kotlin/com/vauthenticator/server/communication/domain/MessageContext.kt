package com.vauthenticator.server.communication.domain

import com.vauthenticator.server.account.domain.Account

typealias MessageContext = Map<String, Any>

fun messageContextFrom(account: Account) =
    mapOf(
        "enabled" to account.enabled,
        "username" to account.username,
        "authorities" to account.authorities,
        "email" to account.email,
        "firstName" to account.firstName,
        "lastName" to account.lastName,
        "birthDate" to account.birthDate?.iso8601FormattedDate().orEmpty(),
        "phone" to account.phone?.formattedPhone().orEmpty()
    )
