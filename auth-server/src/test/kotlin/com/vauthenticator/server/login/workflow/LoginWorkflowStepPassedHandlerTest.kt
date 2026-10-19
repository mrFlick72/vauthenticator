package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.SecurityFixture.principalFor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

class LoginWorkflowStepPassedHandlerTest {

    private val step = object : LoginWorkflowHandler {
        override fun id() = "A_STEP"
        override fun view() = "/a-step"
        override fun isRequiredFor(userName: String, clientAppId: ClientAppId?) = true
    }

    private val uut = LoginWorkflowStepPassedHandler(step)

    @Test
    fun `when a step is passed it is remembered in the session and the authorization request is resumed`() {
        val authorizationRequest = MockHttpServletRequest("GET", "/oauth2/authorize")
            .apply { queryString = "client_id=A_CLIENT_APP_ID" }
        HttpSessionRequestCache().saveRequest(authorizationRequest, MockHttpServletResponse())
        val request = MockHttpServletRequest("POST", "/a-step").apply { setSession(authorizationRequest.session!!) }
        val response = MockHttpServletResponse()

        uut.onAuthenticationSuccess(request, response, principalFor("user@domain.com"))

        assertEquals(setOf("A_STEP"), request.session!!.passedLoginWorkflowSteps())
        assertEquals("http://localhost/oauth2/authorize?client_id=A_CLIENT_APP_ID&continue", response.redirectedUrl)
    }
}
