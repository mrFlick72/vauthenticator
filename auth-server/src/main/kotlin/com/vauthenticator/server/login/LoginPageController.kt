package com.vauthenticator.server.login

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.extentions.hasEnoughScopes
import com.vauthenticator.server.extentions.oauth2ClientId
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationFeatures
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.SessionAttributes
import java.util.*


@Controller
@SessionAttributes("clientId", "features")
class LoginPageController(
    val i18nMessageInjector : I18nMessageInjector,
    val clientApplicationRepository: ClientApplicationRepository,
    val objectMapper: ObjectMapper
) {
    val logger: Logger = LoggerFactory.getLogger(LoginPageController::class.java)

    @GetMapping("/login")
    fun loginPage(session: HttpSession, model: Model, httpServletRequest: HttpServletRequest): String {
        val clientId = session.oauth2ClientId()

        val features = defaultFeature()
        clientAppFeaturesFor(clientId, model, features)

        model.addAttribute("features", objectMapper.writeValueAsString(features))
        model.addAttribute("assetBundle", "login_bundle.js")

        i18nMessageInjector.setMessagedFor(I18nScope.LOGIN_PAGE, model)

        return "template"
    }

    private fun clientAppFeaturesFor(
        clientId: Optional<ClientAppId>,
        model: Model,
        features: MutableMap<String, Boolean>
    ) {
        clientId.ifPresent {
            model.addAttribute("clientId", it.content)
            clientApplicationRepository.findOne(it)
                .map { clientApp ->
                    logger.debug("clientApp.scopes.content: ${clientApp.scopes.content}")
                    features[ClientApplicationFeatures.SIGNUP.value] = clientApp.hasEnoughScopes(Scope.SIGN_UP)
                    features[ClientApplicationFeatures.RESET_PASSWORD.value] =
                        clientApp.hasEnoughScopes(Scope.RESET_PASSWORD)
                }
        }
    }


    private fun defaultFeature() =
        mutableMapOf(
            ClientApplicationFeatures.SIGNUP.value to false,
            ClientApplicationFeatures.RESET_PASSWORD.value to false
        )

}