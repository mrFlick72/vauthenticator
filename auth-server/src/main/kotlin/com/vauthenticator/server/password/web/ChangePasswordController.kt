package com.vauthenticator.server.password.web

import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import com.vauthenticator.server.password.domain.changepassword.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class ChangePasswordController(
    private val i18nMessageInjector: I18nMessageInjector,
    private val accountRepository: AccountRepository,
    private val publisher: ApplicationEventPublisher,
    private val changePassword: ChangePassword,
    private val nextHopeLoginWorkflowSuccessHandler: AuthenticationSuccessHandler,
    private val changePasswordFailureHandler: AuthenticationFailureHandler
) {

    private val logger = LoggerFactory.getLogger(ChangePasswordController::class.java)

    @GetMapping("/change-password")
    fun view(model: Model, httpServletRequest : HttpServletRequest): String {
        i18nMessageInjector.setMessagedFor(I18nScope.CHANGE_PASSWORD_PAGE, model)
        model.addAttribute("assetBundle", "changePassword_bundle.js")
        return "template"
    }

    @PostMapping("/change-password")
    fun resetPassword(
        @RequestParam("new-password") newPassword: String,
        authentication: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        try {
            changePasswordFor(authentication, newPassword)
            nextHopeLoginWorkflowSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        } catch (e: Exception) {
            logger.error(e.message, e)
            val changePasswordException = ChangePasswordException(e.message!!)
            publisher.publishEvent(ChangePasswordFailureEvent(authentication, changePasswordException))
            changePasswordFailureHandler.onAuthenticationFailure(request, response, changePasswordException)
        }

    }

    private fun changePasswordFor(authentication: Authentication, newPassword: String) {
        changePassword.resetPasswordFor(authentication, ChangePasswordRequest(newPassword))
        publisher.publishEvent(ChangePasswordSuccessEvent(authentication))
        resetMandatoryActionToNoAction(authentication)
    }

    private fun resetMandatoryActionToNoAction(authentication: Authentication) {
        accountRepository.accountFor(authentication.name)
            .map {
                accountRepository.save(
                    it.copy(mandatoryAction = AccountMandatoryAction.NO_ACTION)
                )

            }
    }
}
