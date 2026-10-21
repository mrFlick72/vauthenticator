package com.vauthenticator.server.login.workflow

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

/**
 * Remembers in the session that the account holder passed the step, then resumes the authorization request
 * that sent them there, which checks the login workflow again.
 */
class LoginWorkflowStepPassedHandler(
    private val step: LoginWorkflowHandler,
    private val resumeAuthorizationRequest: AuthenticationSuccessHandler
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        request.session.loginWorkflowStepPassed(step)
        resumeAuthorizationRequest.onAuthenticationSuccess(request, response, authentication)
    }
}
