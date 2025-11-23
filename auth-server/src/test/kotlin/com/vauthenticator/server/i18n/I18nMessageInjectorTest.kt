package com.vauthenticator.server.i18n

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.ui.ExtendedModelMap
import org.springframework.ui.Model
import org.springframework.web.servlet.LocaleResolver
import java.util.*

@ExtendWith(MockKExtension::class)
class I18nMessageInjectorTest {
    private val objectMapper = ObjectMapper()
    private val messages = mapOf("prop1" to "value1")
    private val errors = mapOf("error_prop1" to "error_value1")

    @MockK
    private lateinit var localeResolver: LocaleResolver

    @MockK
    private lateinit var i18nMessageRepository: I18nMessageRepository

    @MockK
    private lateinit var currentHttpServletRequestService: CurrentHttpServletRequestService

    private lateinit var model: Model
    private lateinit var uut: I18nMessageInjector

    @BeforeEach
    fun setUp() {
        uut =
            I18nMessageInjector(
                localeResolver,
                objectMapper,
                i18nMessageRepository,
                currentHttpServletRequestService
            )
        model = ExtendedModelMap()
    }

    @Test
    fun `when messages and error are injected but no server side error happen`() {
        howI18nRepositoryGetMessagesFrom(mockHttpServletRequestWithoutServerSideError())
        uut.setMessagedFor(I18nScope.LOGIN_PAGE, model)

        assertEquals(objectMapper.writeValueAsString(messages), model.getAttribute("i18nMessages"))
        assertEquals(objectMapper.writeValueAsString(errors), model.getAttribute("errors"))
        assertEquals(false, model.getAttribute("hasServerSideErrors"))
    }


    @Test
    fun `when messages and error are injected but wit server side error`() {
        howI18nRepositoryGetMessagesFrom(mockHttpServletRequestWithServerSideError())
        uut.setMessagedFor(I18nScope.LOGIN_PAGE, model)

        assertEquals(objectMapper.writeValueAsString(messages), model.getAttribute("i18nMessages"))
        assertEquals(objectMapper.writeValueAsString(errors), model.getAttribute("errors"))
        assertEquals(true, model.getAttribute("hasServerSideErrors"))
    }

    private fun mockHttpServletRequestWithServerSideError(): MockHttpServletRequest {
        val httpServletRequest = MockHttpServletRequest()
        httpServletRequest.setParameter("error")
        httpServletRequest.session?.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", RuntimeException())
        return httpServletRequest
    }

    private fun mockHttpServletRequestWithoutServerSideError() = MockHttpServletRequest()

    private fun howI18nRepositoryGetMessagesFrom(httpServletRequest: MockHttpServletRequest) {
        every { currentHttpServletRequestService.getServletRequest() } returns httpServletRequest
        every { localeResolver.resolveLocale(httpServletRequest) } returns Locale.ENGLISH
        every {
            i18nMessageRepository.getMessagedFor(
                I18nScope.LOGIN_PAGE,
                Locale.ENGLISH.toLanguageTag()
            )
        } returns I18nMessages(messages, errors)
    }


}