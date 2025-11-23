package com.vauthenticator.server.account.domain.welcome

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.communication.domain.EMailSenderService

class SayWelcome(
    private val accountRepository: AccountRepository,
    private val welcomeMailSender: EMailSenderService
) {

    fun welcome(email: String): Unit =
        accountRepository.accountFor(email)
            .map { welcomeMailSender.sendFor(it, emptyMap()) }
            .orElseThrow { AccountNotFoundException("no account with email $email in the database") }

}