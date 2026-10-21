package com.vauthenticator.server.login.workflow

import com.vauthenticator.server.account.domain.AccountMandatoryAction.RESET_PASSWORD
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.mfa.domain.MfaLoginWorkflowHandler
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordLoginWorkflowHandler
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.SecurityFixture.principalFor
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.savedrequest.HttpSessionRequestCache

@ExtendWith(MockKExtension::class)
class LoginWorkflowFilterTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var accountRepository: AccountRepository

    private val account = anAccount()
    private val clientAppId = aClientAppId()
    private val mfaClientApp = aClientApp(clientAppId).copy(scopes = Scopes.from(Scope.MFA_ALWAYS))

    private val request = MockHttpServletRequest("GET", "/oauth2/authorize")
        .apply { addParameter("client_id", clientAppId.content) }
    private val response = MockHttpServletResponse()
    private val chain = MockFilterChain()

    lateinit var mfaStep: MfaLoginWorkflowHandler
    lateinit var changePasswordStep: ChangePasswordLoginWorkflowHandler
    lateinit var uut: LoginWorkflowFilter

    @BeforeEach
    fun setUp() {
        mfaStep = MfaLoginWorkflowHandler(clientApplicationRepository, "/mfa-challenge/send")
        changePasswordStep = ChangePasswordLoginWorkflowHandler(accountRepository)
        uut = LoginWorkflowFilter(
            LoginWorkflowEngine(
                listOf(mfaStep, changePasswordStep),
                HttpSessionRequestCache(),
                DefaultRedirectStrategy(),
                SavedRequestAwareAuthenticationSuccessHandler()
            ),
            PathPatternRequestMatcher.withDefaults().matcher("/oauth2/authorize"),
            AuthenticationTrustResolverImpl(),
            HttpSessionRequestCache(),
            DefaultRedirectStrategy()
        )

        every { accountRepository.accountFor(account.email) } returns account
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `when the mfa is required and not passed in the session the authorization request goes to the mfa challenge`() {
        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { clientApplicationRepository.findOne(clientAppId) } returns mfaClientApp

        uut.doFilter(request, response, chain)

        assertEquals("/mfa-challenge/send", response.redirectedUrl)
        assertNull(chain.request)
        val savedRequest = HttpSessionRequestCache().getRequest(request, response)
        assertEquals(listOf(clientAppId.content), savedRequest?.getParameterValues("client_id")?.toList())
    }

    @Test
    fun `when the mfa is required and already passed in the session the authorization request goes on`() {
        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { clientApplicationRepository.findOne(clientAppId) } returns mfaClientApp
        request.session!!.loginWorkflowStepPassed(mfaStep)

        uut.doFilter(request, response, chain)

        assertNull(response.redirectedUrl)
        assertSame(request, chain.request)
    }

    @Test
    fun `when the client app does not require mfa the authorization request goes on without passing the mfa`() {
        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp(clientAppId)

        uut.doFilter(request, response, chain)

        assertSame(request, chain.request)
        assertEquals(emptySet<String>(), request.session!!.passedLoginWorkflowSteps())
    }

    @Test
    fun `when both mfa and change password are pending the mfa comes first`() {
        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { clientApplicationRepository.findOne(clientAppId) } returns mfaClientApp
        every { accountRepository.accountFor(account.email) } returns account.copy(mandatoryAction = RESET_PASSWORD)

        uut.doFilter(request, response, chain)

        assertEquals("/mfa-challenge/send", response.redirectedUrl)
    }

    @Test
    fun `when the mfa is passed and change password is pending the authorization request goes to change password`() {
        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { clientApplicationRepository.findOne(clientAppId) } returns mfaClientApp
        every { accountRepository.accountFor(account.email) } returns account.copy(mandatoryAction = RESET_PASSWORD)
        request.session!!.loginWorkflowStepPassed(mfaStep)

        uut.doFilter(request, response, chain)

        assertEquals("/change-password", response.redirectedUrl)
        assertNull(chain.request)
    }

    @Test
    fun `when the session is not authenticated the authorization request goes on untouched`() {
        uut.doFilter(request, response, chain)

        assertSame(request, chain.request)
        assertNull(response.redirectedUrl)
    }

    @Test
    fun `when the session is anonymous the authorization request goes on untouched`() {
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))

        uut.doFilter(request, response, chain)

        assertSame(request, chain.request)
        assertNull(response.redirectedUrl)
    }

    @Test
    fun `when the request is not an authorization request it goes on untouched`() {
        val tokenRequest = MockHttpServletRequest("POST", "/oauth2/token")
            .apply { addParameter("client_id", clientAppId.content) }
        SecurityContextHolder.getContext().authentication = principalFor(clientAppId.content)
        every { clientApplicationRepository.findOne(clientAppId) } returns mfaClientApp

        uut.doFilter(tokenRequest, response, chain)

        assertSame(tokenRequest, chain.request)
        assertNull(response.redirectedUrl)
    }
}
