package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account

class AccountLockPasswordLifeCycleStrategyTest : AbstractPasswordLifeCycleStrategyTest() {
    override fun initPasswordLifeCycleStrategy(): PasswordLifeCycleStrategy {
        return AccountLockPasswordLifeCycleStrategy(clock, accountRepository())
    }

    override fun newAccountFrom(account: Account): Account =
        account.copy(accountNonLocked = true)

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.ACCOUNT_LOCK

}