package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.RedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.RequestCache

/**
 * Decides which step of the login workflow the account holder has to pass next.
 */
interface LoginWorkflowEngine {

    /**
     * The first step, in workflow order, that is required for this account and client app and not yet passed in
     * the session, or `null` when there is none and the session is login-complete for the app.
     *
     * @param clientAppId the client app asking, or `null` when no app is asking
     * @param passedSteps the [LoginWorkflowHandler.id]s of the steps already passed in the session
     */
    fun pendingStepFor(userName: String, clientAppId: ClientAppId?, passedSteps: Set<String>): LoginWorkflowHandler?
}

/**
 * The login workflow: its steps in order, and the form login success handler.
 *
 * As [LoginWorkflowEngine] it returns the first required step that is not passed; the steps are evaluated
 * again on every call, nothing remembers a position in the workflow.
 *
 * As the form login success handler a new password login starts the workflow fresh: the passed steps of the
 * session are forgotten, then the authorization request that asked for the login is resumed and
 * [LoginWorkflowFilter] checks it. A login with no client app asking (no saved request) runs the steps that do
 * not depend on an app straight away, and otherwise goes to the default success target.
 *
 * @param handlers the steps, in the order they are presented
 * @param requestCache where the authorization request that asked for the login is saved
 * @param redirectStrategy sends the account holder to a pending step
 * @param defaultSuccessHandler resumes the saved request, or goes to the default target when there is none
 */
class CompositeLoginWorkflowEngine(
    private val handlers: List<LoginWorkflowHandler>,
    private val requestCache: RequestCache,
    private val redirectStrategy: RedirectStrategy,
    private val defaultSuccessHandler: AuthenticationSuccessHandler
) : LoginWorkflowEngine, AuthenticationSuccessHandler {

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
