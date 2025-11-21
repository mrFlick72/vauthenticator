package com.vauthenticator.server.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.events.EventFixture.vauthenticatorAuthEvent
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.login.LoginPageController
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

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

        every { publisher.publishEvent(vauthenticatorAuthEvent) } just runs
        underTest.handle(vauthenticatorAuthEvent.payload as AbstractAuthenticationEvent)

        verify { publisher.publishEvent(vauthenticatorAuthEvent) }
    }
}