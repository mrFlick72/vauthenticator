package com.vauthenticator.server.account.web

import com.vauthenticator.server.account.domain.emailverification.VerifyEMailChallenge
import com.vauthenticator.server.i18n.I18nMessageInjector
import com.vauthenticator.server.i18n.I18nScope
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

@ExtendWith(MockKExtension::class)
internal class MailVerificationControllerTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var mailVerifyEMailChallenge: VerifyEMailChallenge

    @MockK
    lateinit var i18nMessageInjector: I18nMessageInjector

    @BeforeEach
    internal fun setUp() {
        mokMvc = standaloneSetup(MailVerificationController(i18nMessageInjector, mailVerifyEMailChallenge)).build()
    }

    @Test
    internal fun `when the challenge is verified`() {
        every { mailVerifyEMailChallenge.verifyMail("A_TICKET") } just runs
        every { i18nMessageInjector.setMessagedFor(I18nScope.SUCCESSFUL_MAIL_VERIFY_PAGE, any()) } just runs

        mokMvc.perform(MockMvcRequestBuilders.get("/email-verify/A_TICKET"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }
}