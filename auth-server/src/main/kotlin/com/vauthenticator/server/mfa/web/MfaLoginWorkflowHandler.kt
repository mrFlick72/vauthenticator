package com.vauthenticator.server.mfa.web

import com.vauthenticator.server.extentions.hasEnoughScopes
import com.vauthenticator.server.login.workflow.LoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class MfaLoginWorkflowHandler(
    private val clientApplicationRepository: ClientApplicationRepository,
    private val url: String
) : LoginWorkflowHandler {

    override fun view(): String = this.url

    override fun canHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        val clientId = request.session.getAttribute("clientId") as String
        return clientApplicationRepository.findOne(ClientAppId(clientId))
            .filter { it.hasEnoughScopes(Scope.MFA_ALWAYS) }
            .isPresent
    }

}