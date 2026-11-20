package com.vauthenticator.server.password.web

import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import com.vauthenticator.server.page.domain.Page
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import tools.jackson.databind.ObjectMapper

@Controller
class ResetPasswordController(
    private val i18nMessageInjector: I18nMessageInjector,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/reset-password/reset-password-challenge-sender")
    fun resetPasswordChallengeSenderPage(model: Model): String {
        i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_CHALLENGE_SENDER_PAGE, model)
        model.addAttribute("assetBundle", Page.RESET_PASSWORD_CHALLENGE_SENDER.bundle)
        return "template"
    }
    @GetMapping("/reset-password/successful-reset-password-email-challenge")
    fun successfulResetPasswordMailChallengePage(model: Model): String {
        i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_RESET_PASSWORD_CHALLENGE_SENDER_PAGE, model)
        model.addAttribute("assetBundle", Page.SUCCESSFUL_RESET_PASSWORD_MAIL_CHALLENGE.bundle)
        return "template"
    }

    @GetMapping("/reset-password/{ticket}")
    fun resetPasswordPage(@PathVariable ticket: String, model: Model): String {
        val metadata = mapOf("ticket" to ticket)

        i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_PAGE, model)
        model.addAttribute("metadata", objectMapper.writeValueAsString(metadata))
        model.addAttribute("assetBundle", Page.RESET_PASSWORD.bundle)
        return "template"
    }

    @GetMapping("/reset-password/successful-password-reset")
    fun successfulResetPasswordPage(model: Model): String {
        i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_RESET_PASSWORD_PAGE, model)
        model.addAttribute("assetBundle", Page.SUCCESSFUL_PASSWORD_RESET.bundle)
        return "template"
    }
}