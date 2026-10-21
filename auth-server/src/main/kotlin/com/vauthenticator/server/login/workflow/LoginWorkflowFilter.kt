package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.RedirectStrategy
import org.springframework.security.web.savedrequest.RequestCache
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Checks the login workflow on every authorization request: an authenticated session with a pending step
 * is sent to that step, and the authorization request is saved to be resumed once the step is passed.
 */
class LoginWorkflowFilter(
    private val engine: LoginWorkflowEngine,
    private val authorizationRequest: RequestMatcher,
    private val trustResolver: AuthenticationTrustResolver,
    private val requestCache: RequestCache,
    private val redirectStrategy: RedirectStrategy
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest) = !authorizationRequest.matches(request)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
            ?.takeIf { trustResolver.isAuthenticated(it) }
            ?: return filterChain.doFilter(request, response)

        val clientAppId = request.getParameter("client_id")?.let(::ClientAppId)
        val pendingStep =
            engine.pendingStepFor(authentication.name, clientAppId, request.session.passedLoginWorkflowSteps())
        if (pendingStep != null) {
            requestCache.saveRequest(request, response)
            redirectStrategy.sendRedirect(request, response, pendingStep.view())
        } else {
            filterChain.doFilter(request, response)
        }
    }
}
