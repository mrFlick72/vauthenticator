package com.vauthenticator.server.account.web

import com.vauthenticator.server.account.domain.emailverification.VerifyEMailChallenge
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class MailVerificationController(
    private val i18nMessageInjector: I18nMessageInjector,
    private val verifyEMailChallenge: VerifyEMailChallenge
) {

    @GetMapping("/email-verify/{ticket}")
    fun verifyMail(@PathVariable ticket: String, model: Model): String {
        verifyEMailChallenge.verifyMail(ticket)

        i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_MAIL_VERIFY_PAGE, model)
        model.addAttribute("assetBundle", "successfulMailVerify_bundle.js")
        return "template"
    }

}