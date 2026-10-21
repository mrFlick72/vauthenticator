package com.vauthenticator.server.login.workflow

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

/**
 * Remembers in the session that the account holder passed the step, then resumes the authorization request
 * that sent them there, which checks the login workflow again.
 *
 * A step page calls it once the step is passed (the MFA code is verified, the password is changed). There is one
 * instance per step; steps never hand off to each other.
 *
 * @param step the step that was passed
 * @param resumeAuthorizationRequest redirects to the saved authorization request, or to the default target when
 * no client app is asking
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
