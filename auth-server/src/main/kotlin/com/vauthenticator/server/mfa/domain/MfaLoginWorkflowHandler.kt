package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.login.workflow.LoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.ext.hasEnoughScopes

/**
 * The MFA challenge step of the login workflow. It is required by client apps with the `mfa:always` scope and
 * never when no client app is asking; once passed, it counts for every such app for the rest of the session
 * (a second-factor-verified session).
 *
 * @param url the page that sends the challenge to the default device and shows the MFA page
 */
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
