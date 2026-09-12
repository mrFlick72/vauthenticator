package com.vauthenticator.server.password.api

import com.vauthenticator.server.password.domain.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.PasswordLifeCycleRule
import com.vauthenticator.server.password.domain.PasswordLifeCycleStrategyExecutor
import com.vauthenticator.server.support.MfaFixture.account
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
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
import java.time.Duration

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleEndPointTest {

    private val objectMapper = ObjectMapper()

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var passwordLifeCycleStrategyExecutor: PasswordLifeCycleStrategyExecutor

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            PasswordLifeCycleEndPoint(passwordLifeCycleStrategyExecutor)
        ).build()
    }

    @Test
    fun `when a new password policy is set`() {
        val anAccount = account
        val passwordLifeCycleRule = PasswordLifeCycleRule(
            userName = anAccount.email,
            ttl = Duration.ofHours(1),
            action = PasswordLifeCycleAction.PASSWORD_RESET
        )
        val m2mPrincipal = m2mPrincipalFor("m2m", listOf("SCOPE_ADMIN"))
        every { passwordLifeCycleStrategyExecutor.execute(passwordLifeCycleRule) } just runs

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

        verify { passwordLifeCycleStrategyExecutor.execute(passwordLifeCycleRule) }
    }
}