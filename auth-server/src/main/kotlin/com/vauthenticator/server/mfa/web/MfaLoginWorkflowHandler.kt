package com.vauthenticator.server.mfa.web

import com.vauthenticator.server.login.workflow.LoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.ext.hasEnoughScopes

class MfaLoginWorkflowHandler(
    private val clientApplicationRepository: ClientApplicationRepository,
    private val url: String
) : LoginWorkflowHandler {

    override fun id(): String = "MFA"

    override fun view(): String = this.url

    override fun isRequiredFor(userName: String, clientAppId: ClientAppId?): Boolean =
        clientAppId
            ?.let { clientApplicationRepository.findOne(it) }
            ?.hasEnoughScopes(Scope.MFA_ALWAYS)
            ?: false

}
