package com.vauthenticator.server.account.web

import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.SessionAttributes

@Controller
@SessionAttributes("features")
class SignUpController(val i18nMessageInjector: I18nMessageInjector) {

    @GetMapping("/sign-up")
    fun signUp(@ModelAttribute("features") features: List<String>, model: Model): String {
        model.addAttribute("assetBundle", "signup_bundle.js")
        i18nMessageInjector.setMessagedFor(I18nScope.SIGN_UP_PAGE, model)

        return "template"
    }

    @GetMapping("/sign-up/succeeded")
    fun successfulSignUp(@ModelAttribute("features") features: List<String>, model: Model): String {
        model.addAttribute("assetBundle", "successfulSignUp_bundle.js")
        i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_SIGN_UP_PAGE, model)

        return "template"
    }
}