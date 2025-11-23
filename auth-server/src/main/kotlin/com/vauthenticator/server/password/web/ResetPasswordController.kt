package com.vauthenticator.server.password.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ResetPasswordController(
    private val i18nMessageInjector: I18nMessageInjector,
    private val objectMapper: ObjectMapper
) {

    @GetMapping("/reset-password/reset-password-challenge-sender")
    fun resetPasswordChallengeSenderPage(model: Model): String {
        i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_CHALLENGE_SENDER_PAGE, model)
        model.addAttribute("assetBundle", "resetPasswordChallengeSender_bundle.js")
        return "template"
    }
    @GetMapping("/reset-password/successful-reset-password-email-challenge")
    fun successfulResetPasswordMailChallengePage(model: Model): String {
        i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_RESET_PASSWORD_CHALLENGE_SENDER_PAGE, model)
        model.addAttribute("assetBundle", "successfulResetPasswordMailChallenge_bundle.js")
        return "template"
    }

    @GetMapping("/reset-password/{ticket}")
    fun resetPasswordPage(@PathVariable ticket: String, model: Model): String {
        val metadata = mapOf("ticket" to ticket)

        i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_PAGE, model)
        model.addAttribute("metadata", objectMapper.writeValueAsString(metadata))
        model.addAttribute("assetBundle", "resetPassword_bundle.js")
        return "template"
    }

    @GetMapping("/reset-password/successful-password-reset")
    fun successfulResetPasswordPage(model: Model): String {
        i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_RESET_PASSWORD_PAGE, model)
        model.addAttribute("assetBundle", "successfulPasswordReset_bundle.js")
        return "template"
    }
}