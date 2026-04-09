package com.vauthenticator.server.account.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallenge
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.web.ExceptionAdviceController
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

private const val ENDPOINT = "/api/verify-challenge"

@ExtendWith(MockKExtension::class)
class EMailVerificationEndPointTest {

    private val objectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @MockK
    private lateinit var sendVerifyEMailChallenge: SendVerifyEMailChallenge

    @MockK
    private lateinit var clientApplicationRepository: ClientApplicationRepository

    private val principal = principalFor(
        A_CLIENT_APP_ID,
        EMAIL,
        emptyList(),
        listOf(Scope.MAIL_VERIFY.content)
    )

    @BeforeEach
    fun setUp() {
        mockMvc = standaloneSetup(
            EMailVerificationEndPoint(
                PermissionValidator(clientApplicationRepository),
                sendVerifyEMailChallenge
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `when the verification challenge is sent`() {
        every { sendVerifyEMailChallenge.sendVerifyMail(EMAIL) } just runs

        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(EMailVerificationRequest(EMAIL)))
                .principal(principal)
        )
            .andExpect(status().isNoContent)

        verify { sendVerifyEMailChallenge.sendVerifyMail(EMAIL) }
    }

    @Test
    fun `when the request body is missing`() {
        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(principal)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { sendVerifyEMailChallenge.sendVerifyMail(any()) }
    }

    @Test
    fun `when the email field is missing`() {
        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(emptyMap<String, String>()))
                .principal(principal)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { sendVerifyEMailChallenge.sendVerifyMail(any()) }
    }

    @Test
    fun `when the principal does not have the mail verify scope`() {
        val principalWithoutScope = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            emptyList(),
            emptyList()
        )

        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(EMailVerificationRequest(EMAIL)))
                .principal(principalWithoutScope)
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { sendVerifyEMailChallenge.sendVerifyMail(any()) }
    }
}
