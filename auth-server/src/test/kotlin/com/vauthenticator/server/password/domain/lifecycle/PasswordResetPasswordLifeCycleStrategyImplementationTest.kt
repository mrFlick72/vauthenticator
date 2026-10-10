package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.support.AccountTestFixture.anAccount

class PasswordResetPasswordLifeCycleStrategyImplementationTest : AbstractPasswordLifeCycleStrategyTest() {

    override fun initImplementation(): PasswordLifeCycleStrategyImplementation =
        PasswordResetPasswordLifeCycleStrategyImplementation()

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.PASSWORD_RESET

    override fun accountWithoutActionInEffect(): Account =
        anAccount().copy(accountNonLocked = true, mandatoryAction = AccountMandatoryAction.NO_ACTION)

    override fun accountWithActionApplied(account: Account): Account =
        account.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD)
}
