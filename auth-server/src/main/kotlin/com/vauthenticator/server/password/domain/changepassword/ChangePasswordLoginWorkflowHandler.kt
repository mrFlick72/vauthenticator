package com.vauthenticator.server.password.domain.changepassword

import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.login.workflow.LoginWorkflowHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

const val CHANGE_PASSWORD_URL = "/change-password"

class ChangePasswordLoginWorkflowHandler(
    val accountRepository: AccountRepository,
    val handler: AuthenticationSuccessHandler
) : LoginWorkflowHandler {

    override fun view(): String = CHANGE_PASSWORD_URL

    override fun canHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        val username = getCurrentLoggedInUserName()
        return username?.let {
            accountRepository.accountFor(username)?.mandatoryAction === AccountMandatoryAction.RESET_PASSWORD
        }
            ?: false
    }

    private fun getCurrentLoggedInUserName(): String? = SecurityContextHolder.getContext().authentication?.name

}
