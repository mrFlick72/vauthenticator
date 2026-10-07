package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.support.AccountTestFixture.anAccount

class AccountLockPasswordLifeCycleStrategyImplementationTest : AbstractPasswordLifeCycleStrategyTest() {

    override fun initImplementation(): PasswordLifeCycleStrategyImplementation =
        AccountLockPasswordLifeCycleStrategyImplementation()

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.ACCOUNT_LOCK

    override fun accountWithoutActionInEffect(): Account =
        anAccount().copy(accountNonLocked = true)

    override fun accountWithActionApplied(account: Account): Account =
        account.copy(accountNonLocked = false)
}
