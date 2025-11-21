package com.vauthenticator.server.password.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.password.domain.PasswordPolicyViolation
import com.vauthenticator.server.password.domain.changepassword.ChangePassword
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordRequest
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class ChangePasswordEndPointTest {
    private val objectMapper = ObjectMapper()

    lateinit var mokMvc: MockMvc

    val clientAppId = ClientAppId(A_CLIENT_APP_ID)


    @MockK
    lateinit var changePassword: ChangePassword

    @BeforeEach
    fun setUp() {
        mokMvc =
            MockMvcBuilders.standaloneSetup(
                ChangePasswordEndPoint(changePassword)
            )
                .build()
    }

    @Test
    fun `when a change password attempt is executed`() {
        val principal = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            listOf(VAUTHENTICATOR_ADMIN),
            listOf(Scope.RESET_PASSWORD.content)
        )
        every { changePassword.resetPasswordFor(principal, ChangePasswordRequest("it is a new password")) } just runs

        mokMvc.perform(
            put("/api/accounts/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("pwd" to "it is a new password")))
                .principal(principal)
        )
            .andExpect(status().isNoContent)

        verify { changePassword.resetPasswordFor(principal, ChangePasswordRequest("it is a new password")) }

    }

    @Test
    fun `when a change password for an account not found`() {
        val principal = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            listOf(VAUTHENTICATOR_ADMIN),
            listOf(Scope.RESET_PASSWORD.content)
        )
        every {
            changePassword.resetPasswordFor(
                principal,
                ChangePasswordRequest("it is a new password")
            )
        } throws AccountNotFoundException("")

        mokMvc.perform(
            put("/api/accounts/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("pwd" to "it is a new password")))
                .principal(principal)
        )
            .andExpect(status().isInternalServerError)

        verify { changePassword.resetPasswordFor(principal, ChangePasswordRequest("it is a new password")) }

    }

    @Test
    fun `when a change password that do ntoo meet the security requirements`() {
        val principal = principalFor(
            A_CLIENT_APP_ID,
            EMAIL,
            listOf(VAUTHENTICATOR_ADMIN),
            listOf(Scope.RESET_PASSWORD.content)
        )
        every {
            changePassword.resetPasswordFor(
                principal,
                ChangePasswordRequest("it is a new password")
            )
        } throws PasswordPolicyViolation("")

        mokMvc.perform(
            put("/api/accounts/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("pwd" to "it is a new password")))
                .principal(principal)
        )
            .andExpect(status().isInternalServerError)

        verify { changePassword.resetPasswordFor(principal, ChangePasswordRequest("it is a new password")) }

    }


}