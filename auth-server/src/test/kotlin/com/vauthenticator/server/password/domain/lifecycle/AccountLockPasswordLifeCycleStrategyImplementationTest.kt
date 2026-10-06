package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account

class AccountLockPasswordLifeCycleStrategyImplementationTest : AbstractPasswordLifeCycleStrategyTest() {
    override fun initPasswordLifeCycleStrategy(): PasswordLifeCycleStrategy {
        return BasePasswordLifeCycleStrategy(clock, accountRepository(), passwordLifeCycleRepository(), AccountLockPasswordLifeCycleStrategyImplementation(accountRepository()))
    }

    override fun newAccountFrom(account: Account): Account =
        account.copy(accountNonLocked = false)

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.ACCOUNT_LOCK

}