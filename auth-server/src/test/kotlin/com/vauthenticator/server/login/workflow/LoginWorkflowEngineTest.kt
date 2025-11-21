package com.vauthenticator.server.login.workflow

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

@ExtendWith(MockKExtension::class)
class LoginWorkflowEngineTest {

    @MockK
    lateinit var defaultSuccessHandler: AuthenticationSuccessHandler

    @MockK
    lateinit var firstLoginWorkflowHandler: LoginWorkflowHandler

    @MockK
    lateinit var secondLoginWorkflowHandler: LoginWorkflowHandler

    @MockK
    lateinit var session: HttpSession

    @MockK
    lateinit var request: HttpServletRequest

    @Test
    fun `when a login workflow runs`() {
        val loginWorkflowHandlers = listOf(
            firstLoginWorkflowHandler,
            secondLoginWorkflowHandler
        )

        every { session.getAttribute("CompositeLoginWorkflowEngine_index") } returns 0 andThen 1 andThen 2
        every { session.setAttribute("CompositeLoginWorkflowEngine_index", 1) } just runs
        every { session.setAttribute("CompositeLoginWorkflowEngine_index", 2) } just runs

        val uut = CompositeLoginWorkflowEngine(loginWorkflowHandlers, defaultSuccessHandler)

        assertSame(firstLoginWorkflowHandler, uut.workflowsNextHop(session))
        assertSame(secondLoginWorkflowHandler, uut.workflowsNextHop(session))
        assertSame(DefaultLoginWorkflowHandler, uut.workflowsNextHop(session))
    }
}