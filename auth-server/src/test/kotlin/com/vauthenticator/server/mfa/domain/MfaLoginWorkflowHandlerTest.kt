package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MfaLoginWorkflowHandlerTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val userName = "user@domain.com"
    private val clientAppId = ClientAppFixture.aClientAppId()
    private val clientApp = ClientAppFixture.aClientApp(clientAppId)

    @Test
    fun `when the mfa is required`() {
        val clientApp = clientApp.copy(scopes = Scopes.from(Scope.MFA_ALWAYS))
        every { clientApplicationRepository.findOne(clientAppId) } returns clientApp

        val uut = MfaLoginWorkflowHandler(clientApplicationRepository, "")

        assertTrue(uut.isRequiredFor(userName, clientAppId))
    }

    @Test
    fun `when the mfa is not required`() {
        every { clientApplicationRepository.findOne(clientAppId) } returns clientApp

        val uut = MfaLoginWorkflowHandler(clientApplicationRepository, "")

        assertFalse(uut.isRequiredFor(userName, clientAppId))
    }

    @Test
    fun `when no client app is asking the mfa is not required`() {
        val uut = MfaLoginWorkflowHandler(clientApplicationRepository, "")

        assertFalse(uut.isRequiredFor(userName, null))
    }
}
