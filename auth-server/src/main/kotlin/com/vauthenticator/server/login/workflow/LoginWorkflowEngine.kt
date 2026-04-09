package com.vauthenticator.server.login.workflow

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

interface LoginWorkflowEngine {
    fun workflowsNextHop(session: HttpSession): LoginWorkflowHandler
    fun workflowsHasNextHop(session: HttpSession): Boolean
}

class CompositeLoginWorkflowEngine(
    private val handlers: List<LoginWorkflowHandler>,
    private val defaultSuccessHandler: AuthenticationSuccessHandler
) : LoginWorkflowEngine, AuthenticationSuccessHandler {

    private val logger = LoggerFactory.getLogger(CompositeLoginWorkflowEngine::class.java)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        this.defaultSuccessHandler.onAuthenticationSuccess(request, response, authentication)
    }

    override fun workflowsNextHop(session: HttpSession): LoginWorkflowHandler {
        val index = (session.getAttribute("CompositeLoginWorkflowEngine_index") as? Int) ?: 0
        val nextHandlerIndex = index + 1

        logger.debug("CompositeLoginWorkflowEngine_index $nextHandlerIndex")
        return if (nextHandlerIndex > handlers.size) {
            logger.debug("go to DefaultLoginWorkflowHandler")
            DefaultLoginWorkflowHandler
        } else {
            session.setAttribute("CompositeLoginWorkflowEngine_index", nextHandlerIndex)
            val loginWorkflowHandler = handlers[index]
            logger.debug("evaluate loginWorkflowHandler $loginWorkflowHandler")
            loginWorkflowHandler
        }

    }

    override fun workflowsHasNextHop(session: HttpSession): Boolean {
        val index = (session.getAttribute("CompositeLoginWorkflowEngine_index") as? Int) ?: 0
        return index < handlers.size
    }

}
