package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account

class AccountLockPasswordLifeCycleStrategyImplementationTest : AbstractPasswordLifeCycleStrategyTest() {
    override fun initPasswordLifeCycleStrategy(): PasswordLifeCycleStrategy {
        return BasePasswordLifeCycleStrategy(clock, accountRepository(), AccountLockPasswordLifeCycleStrategyImplementation(accountRepository()))
    }

    override fun newAccountFrom(account: Account): Account =
        account.copy(accountNonLocked = true)

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.ACCOUNT_LOCK

}