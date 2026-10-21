package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.account.domain.AccountMandatoryAction.RESET_PASSWORD
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mfa.domain.MfaLoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordLoginWorkflowHandler
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.SecurityFixture.principalFor
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

@ExtendWith(MockKExtension::class)
class CompositeLoginWorkflowEngineTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var accountRepository: AccountRepository

    private val account = anAccount()
    private val authentication = principalFor(account.email)
    private val request = MockHttpServletRequest("POST", "/login")
    private val response = MockHttpServletResponse()

    lateinit var mfaStep: MfaLoginWorkflowHandler
    lateinit var uut: CompositeLoginWorkflowEngine

    @BeforeEach
    fun setUp() {
        mfaStep = MfaLoginWorkflowHandler(clientApplicationRepository, "/mfa-challenge/send")
        uut = CompositeLoginWorkflowEngine(
            listOf(mfaStep, ChangePasswordLoginWorkflowHandler(accountRepository)),
            HttpSessionRequestCache(),
            DefaultRedirectStrategy(),
            SavedRequestAwareAuthenticationSuccessHandler()
        )

        every { accountRepository.accountFor(account.email) } returns account
    }

    @Test
    fun `when the password is proven again the passed steps are forgotten and the authorization request is resumed`() {
        val authorizationRequest = MockHttpServletRequest("GET", "/oauth2/authorize")
            .apply { queryString = "client_id=A_CLIENT_APP_ID" }
        HttpSessionRequestCache().saveRequest(authorizationRequest, MockHttpServletResponse())
        request.setSession(authorizationRequest.session!!)
        request.session!!.loginWorkflowStepPassed(mfaStep)

        uut.onAuthenticationSuccess(request, response, authentication)

        assertEquals(emptySet<String>(), request.session!!.passedLoginWorkflowSteps())
        assertEquals("http://localhost/oauth2/authorize?client_id=A_CLIENT_APP_ID&continue", response.redirectedUrl)
    }

    @Test
    fun `when no client app is asking the steps that do not depend on an app run straight away`() {
        every { accountRepository.accountFor(account.email) } returns account.copy(mandatoryAction = RESET_PASSWORD)

        uut.onAuthenticationSuccess(request, response, authentication)

        assertEquals("/change-password", response.redirectedUrl)
    }

    @Test
    fun `when a client app is asking the authorization request is resumed even with a pending step`() {
        every { accountRepository.accountFor(account.email) } returns account.copy(mandatoryAction = RESET_PASSWORD)
        val authorizationRequest = MockHttpServletRequest("GET", "/oauth2/authorize")
            .apply { queryString = "client_id=A_CLIENT_APP_ID" }
        HttpSessionRequestCache().saveRequest(authorizationRequest, MockHttpServletResponse())
        request.setSession(authorizationRequest.session!!)

        uut.onAuthenticationSuccess(request, response, authentication)

        assertEquals("http://localhost/oauth2/authorize?client_id=A_CLIENT_APP_ID&continue", response.redirectedUrl)
    }

    @Test
    fun `when no client app is asking and no step is pending the login goes to the default page`() {
        uut.onAuthenticationSuccess(request, response, authentication)

        assertEquals("/", response.redirectedUrl)
    }

    @Test
    fun `when a step is passed it is remembered in the session and the authorization request is resumed`() {
        val authorizationRequest = MockHttpServletRequest("GET", "/oauth2/authorize")
            .apply { queryString = "client_id=A_CLIENT_APP_ID" }
        HttpSessionRequestCache().saveRequest(authorizationRequest, MockHttpServletResponse())
        val stepRequest = MockHttpServletRequest("POST", "/mfa-challenge").apply { setSession(authorizationRequest.session!!) }

        uut.stepPassed(mfaStep, stepRequest, response, authentication)

        assertEquals(setOf("MFA"), stepRequest.session!!.passedLoginWorkflowSteps())
        assertEquals("http://localhost/oauth2/authorize?client_id=A_CLIENT_APP_ID&continue", response.redirectedUrl)
    }
}
