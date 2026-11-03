package com.vauthenticator.server.events

import com.vauthenticator.server.events.EventFixture.vauthenticatorAuthEvent
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.login.LoginPageController
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.authentication.event.LogoutSuccessEvent
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import tools.jackson.databind.ObjectMapper

@ExtendWith(MockKExtension::class)
class SpringEventEventsDispatcherTest {
    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository
    @MockK
    lateinit var  i18nMessageInjector : I18nMessageInjector

    @BeforeEach
    internal fun setUp() {
        mokMvc =
            MockMvcBuilders.standaloneSetup(
                LoginPageController(i18nMessageInjector, clientApplicationRepository, ObjectMapper())
            ).build()
    }

    @MockK
    private lateinit var publisher: ApplicationEventPublisher

    @Test
    fun `happy path`() {
        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addParameter("client_id", vauthenticatorAuthEvent.clientAppId.content)
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(mockHttpServletRequest))

        val underTest = SpringEventEventsDispatcher(publisher)

        every { publisher.publishEvent(any<VAuthenticatorAuthEvent>()) } just runs
        underTest.handle(vauthenticatorAuthEvent.payload as AbstractAuthenticationEvent)

        verify {
            publisher.publishEvent(match<VAuthenticatorAuthEvent> {
                it.clientAppId == vauthenticatorAuthEvent.clientAppId && it.userName == vauthenticatorAuthEvent.userName
            })
        }
    }

    @Test
    fun `a password login without a client app is dispatched with no client app`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        val underTest = SpringEventEventsDispatcher(publisher)

        every { publisher.publishEvent(any<VAuthenticatorAuthEvent>()) } just runs
        underTest.handle(vauthenticatorAuthEvent.payload as AbstractAuthenticationEvent)

        verify {
            publisher.publishEvent(match<VAuthenticatorAuthEvent> {
                it.clientAppId == ClientAppId.empty() && it.userName == vauthenticatorAuthEvent.userName
            })
        }
    }

    @Test
    fun `an authentication that is not a login and has no client app, such as a bearer token, is not dispatched`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        val underTest = SpringEventEventsDispatcher(publisher)

        underTest.handle(AuthenticationSuccessEvent(BearerTokenAuthenticationToken("A_TOKEN")))

        verify(exactly = 0) { publisher.publishEvent(any()) }
    }

    @Test
    fun `a logout without a client app is dispatched with no client app`() {
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(MockHttpServletRequest()))
        val underTest = SpringEventEventsDispatcher(publisher)
        val authentication = UsernamePasswordAuthenticationToken.authenticated("anemail@domain.com", null, emptyList())

        every { publisher.publishEvent(any<VAuthenticatorAuthEvent>()) } just runs
        underTest.handle(LogoutSuccessEvent(authentication))

        verify {
            publisher.publishEvent(match<VAuthenticatorAuthEvent> {
                it.clientAppId == ClientAppId.empty() && it.payload is LogoutSuccessEvent
            })
        }
    }

    @Test
    fun `a password login outside any request is dispatched with no client app`() {
        val underTest = SpringEventEventsDispatcher(publisher)

        every { publisher.publishEvent(any<VAuthenticatorAuthEvent>()) } just runs
        underTest.handle(vauthenticatorAuthEvent.payload as AbstractAuthenticationEvent)

        verify { publisher.publishEvent(match<VAuthenticatorAuthEvent> { it.clientAppId == ClientAppId.empty() }) }
    }

    // request attributes are bound to the thread: start and end every test without any
    @BeforeEach
    @AfterEach
    fun resetRequestAttributes() {
        RequestContextHolder.resetRequestAttributes()
    }
}