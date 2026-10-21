package com.vauthenticator.server.password.domain.changepassword

import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.login.workflow.LoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId

const val CHANGE_PASSWORD_URL = "/change-password"

/**
 * The mandatory password change step of the login workflow. It is required while the account has the
 * `RESET_PASSWORD` mandatory action, for any client app and also when none is asking, so it runs right after a
 * direct login too.
 */
class ChangePasswordLoginWorkflowHandler(
    val accountRepository: AccountRepository
) : LoginWorkflowHandler {

    override fun id(): String = "CHANGE_PASSWORD"

    override fun view(): String = CHANGE_PASSWORD_URL

    override fun isRequiredFor(userName: String, clientAppId: ClientAppId?): Boolean =
        accountRepository.accountFor(userName)?.mandatoryAction === AccountMandatoryAction.RESET_PASSWORD

}
