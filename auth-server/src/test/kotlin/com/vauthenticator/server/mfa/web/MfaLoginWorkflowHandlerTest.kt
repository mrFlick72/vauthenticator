package com.vauthenticator.server.mfa.web

import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class MfaLoginWorkflowHandlerTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var request: HttpServletRequest

    @MockK
    lateinit var resposnse: HttpServletResponse

    @MockK
    lateinit var session: HttpSession

    private val clientAppId = ClientAppFixture.aClientAppId()
    private val clientApp = ClientAppFixture.aClientApp(clientAppId)

    @Test
    fun `when the mfa is required`() {
        val clientApp = clientApp.copy(scopes = Scopes.from(Scope.MFA_ALWAYS))

        val uut = MfaLoginWorkflowHandler(clientApplicationRepository, "")

        given(clientAppId, clientApp)

        val actual = uut.canHandle(request, resposnse)

        assertTrue(actual)

        verifyExpectationFor(clientAppId)
    }

    @Test
    fun `when the mfa is not required`() {
        val clientApp = ClientAppFixture.aClientApp(clientAppId)

        val uut = MfaLoginWorkflowHandler(clientApplicationRepository, "")

        given(clientAppId, clientApp)

        val actual = uut.canHandle(request, resposnse)

        assertFalse(actual)

        verifyExpectationFor(clientAppId)
    }

    private fun given(
        clientAppId: ClientAppId,
        clientApp: ClientApplication
    ) {
        every { request.session } returns session
        every { session.getAttribute("clientId") } returns clientAppId.content
        every { clientApplicationRepository.findOne(clientAppId) } returns Optional.of(clientApp)
    }

    private fun verifyExpectationFor(clientAppId: ClientAppId) {
        verify { request.session }
        verify { session.getAttribute("clientId") }
        verify { clientApplicationRepository.findOne(clientAppId) }
    }
}