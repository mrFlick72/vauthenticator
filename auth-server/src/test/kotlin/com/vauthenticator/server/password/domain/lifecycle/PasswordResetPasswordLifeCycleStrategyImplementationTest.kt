package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction


class PasswordResetPasswordLifeCycleStrategyImplementationTest : AbstractPasswordLifeCycleStrategyTest() {

    override fun initPasswordLifeCycleStrategy(): PasswordLifeCycleStrategy {
        return BasePasswordLifeCycleStrategy(clock, accountRepository(), passwordLifeCycleRepository(), PasswordResetPasswordLifeCycleStrategyImplementation(accountRepository()))
    }


    override fun newAccountFrom(account: Account): Account =
        account.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD)

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.PASSWORD_RESET

}
