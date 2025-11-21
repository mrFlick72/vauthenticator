package com.vauthenticator.server.password.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class ResetPasswordControllerTest {
    lateinit var mokMvc: MockMvc

    val objectMapper = ObjectMapper()

    @MockK
    lateinit var i18nMessageInjector: I18nMessageInjector

    @BeforeEach
    fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(ResetPasswordController(i18nMessageInjector, ObjectMapper()))
            .build()
    }

    @Test
    fun `when the reset password challenge page is shown`() {
        every { i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_CHALLENGE_SENDER_PAGE, any()) } just runs

        mokMvc.perform(get("/reset-password/reset-password-challenge-sender"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("assetBundle", "resetPasswordChallengeSender_bundle.js"))
            .andExpect(view().name("template"))

        verify { i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_CHALLENGE_SENDER_PAGE, any()) }
    }

    @Test
    fun `when the successful reset password challenge page is shown`() {
        every {
            i18nMessageInjector.setMessagedFor(
                I18nScope.SUCCESSFUL_RESET_PASSWORD_CHALLENGE_SENDER_PAGE,
                any()
            )
        } just runs

        mokMvc.perform(get("/reset-password/successful-reset-password-email-challenge"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("assetBundle", "successfulResetPasswordMailChallenge_bundle.js"))
            .andExpect(view().name("template"))

        verify { i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_RESET_PASSWORD_CHALLENGE_SENDER_PAGE, any()) }

    }

    @Test
    fun `when the reset password page is shown`() {
        every { i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_PAGE, any()) } just runs

        mokMvc.perform(get("/reset-password/{ticket}", "A_TICKET"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("assetBundle", "resetPassword_bundle.js"))
            .andExpect(model().attribute("metadata", objectMapper.writeValueAsString(mapOf("ticket" to "A_TICKET"))))
            .andExpect(view().name("template"))

        verify { i18nMessageInjector.setMessagedFor(I18nScope.RESET_PASSWORD_PAGE, any()) }
    }

    @Test
    fun `when the successful reset password page is shown`() {
        every {
            i18nMessageInjector.setMessagedFor(
                I18nScope.SUCCESSFUL_RESET_PASSWORD_PAGE,
                any()
            )
        } just runs

        mokMvc.perform(get("/reset-password/successful-password-reset"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("assetBundle", "successfulPasswordReset_bundle.js"))
            .andExpect(view().name("template"))

        verify { i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_RESET_PASSWORD_PAGE, any()) }

    }
}