package com.vauthenticator.server.password.api

import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleExecutor
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.MfaFixture.account
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.support.passwordLifeCycleRule
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
import tools.jackson.databind.ObjectMapper

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleEndPointTest {

    private val objectMapper = ObjectMapper()

    lateinit var mokMvc: MockMvc
    @MockK
    lateinit var passwordLifeCycleExecutor: PasswordLifeCycleExecutor

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            PasswordLifeCycleEndPoint(
                PermissionValidator(clientApplicationRepository),
                passwordLifeCycleExecutor
            )
        ).setControllerAdvice(ExceptionAdviceController())
            .build()
    }

    @Test
    fun `when a new password policy is set`() {
        val m2mPrincipal = m2mPrincipalFor("m2m", listOf("admin:password-lifecycle-editor"))
        every { passwordLifeCycleExecutor.register(passwordLifeCycleRule) } just runs

        mokMvc.perform(
            put("/api/admin/accounts/password/lifecycle")
                .principal(m2mPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        passwordLifeCycleRule
                    )
                )
        )
            .andExpect { status().isNoContent }

        verify { passwordLifeCycleExecutor.register(passwordLifeCycleRule) }
    }

    @Test
    fun `when a new password policy fails for permission constraints`() {
        val m2mPrincipal = m2mPrincipalFor("m2m", listOf("admin:whatever"))

        mokMvc.perform(
            put("/api/admin/accounts/password/lifecycle")
                .principal(m2mPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        passwordLifeCycleRule
                    )
                )
        )
            .andExpect { status().isForbidden }

        verify(exactly = 0) { passwordLifeCycleExecutor.register(passwordLifeCycleRule) }
    }
}