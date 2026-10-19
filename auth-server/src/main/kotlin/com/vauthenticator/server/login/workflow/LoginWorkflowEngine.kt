package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

interface LoginWorkflowEngine {
    fun pendingStepFor(userName: String, clientAppId: ClientAppId?, passedSteps: Set<String>): LoginWorkflowHandler?
}

/**
 * The login workflow: its steps in order, and the form login success handler. A new password login starts the
 * workflow fresh; the authorization request that asked for the login checks it, and a login with no client app
 * asking runs the steps that do not depend on an app straight away.
 */
class CompositeLoginWorkflowEngine(
    private val handlers: List<LoginWorkflowHandler>
) : LoginWorkflowEngine, AuthenticationSuccessHandler {

    private val requestCache = HttpSessionRequestCache()
    private val redirectStrategy = DefaultRedirectStrategy()
    private val defaultSuccessHandler = SavedRequestAwareAuthenticationSuccessHandler()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        request.session.forgetPassedLoginWorkflowSteps()

        val noClientAppIsAsking = requestCache.getRequest(request, response) == null
        val pendingStep = if (noClientAppIsAsking) pendingStepFor(authentication.name, null, emptySet()) else null
        if (pendingStep != null) {
            redirectStrategy.sendRedirect(request, response, pendingStep.view())
        } else {
            defaultSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        }
    }

    override fun pendingStepFor(
        userName: String,
        clientAppId: ClientAppId?,
        passedSteps: Set<String>
    ): LoginWorkflowHandler? =
        handlers.firstOrNull { it.id() !in passedSteps && it.isRequiredFor(userName, clientAppId) }

}
