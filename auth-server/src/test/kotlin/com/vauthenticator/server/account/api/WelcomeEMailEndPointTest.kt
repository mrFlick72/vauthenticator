package com.vauthenticator.server.account.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.welcome.SayWelcome
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
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

private const val ENDPOINT = "/api/sign-up/welcome"

@ExtendWith(MockKExtension::class)
class WelcomeEMailEndPointTest {

    private val objectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @MockK
    private lateinit var sayWelcome: SayWelcome

    @MockK
    private lateinit var clientApplicationRepository: ClientApplicationRepository

    private val principal = principalFor(
        A_CLIENT_APP_ID,
        EMAIL,
        listOf(VAUTHENTICATOR_ADMIN),
        listOf(Scope.WELCOME.content)
    )

    @BeforeEach
    fun setUp() {
        mockMvc = standaloneSetup(
            WelcomeEMailEndPoint(
                PermissionValidator(clientApplicationRepository),
                sayWelcome
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `when welcome mail is sent`() {
        every { sayWelcome.welcome(EMAIL) } just runs

        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("email" to EMAIL)))
                .principal(principal)
        )
            .andExpect(status().isNoContent)

        verify { sayWelcome.welcome(EMAIL) }
    }

    @Test
    fun `when the account does not exist`() {
        every { sayWelcome.welcome(EMAIL) } throws AccountNotFoundException("")

        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("email" to EMAIL)))
                .principal(principal)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `when the request body is missing`() {
        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(principal)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { sayWelcome.welcome(any()) }
    }

    @Test
    fun `when the request body does not contains the email field`() {
        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("some-key" to "irrelevant")))
                .principal(principal)
        )
            .andExpect(status().isBadRequest)

        verify(exactly = 0) { sayWelcome.welcome(any()) }
    }

    @Test
    fun `when the principal does not have the welcome scope`() {
        val principalWithoutScope = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            emptyList(),
            emptyList()
        )

        mockMvc.perform(
            put(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("email" to EMAIL)))
                .principal(principalWithoutScope)
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { sayWelcome.welcome(any()) }
    }
}
