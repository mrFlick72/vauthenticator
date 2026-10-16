package com.vauthenticator.server.password.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.password.domain.PasswordGenerator
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
import com.vauthenticator.server.web.ExceptionAdviceController
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

@ExtendWith(MockKExtension::class)
class PasswordGeneratorEndPointTest {

    private val objectMapper = ObjectMapper()

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var passwordGenerator: PasswordGenerator

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            PasswordGeneratorEndPoint(PermissionValidator(clientApplicationRepository), passwordGenerator)
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `when a new random password is generated`() {
        every { passwordGenerator.generate() } returns "A_RANDOM_PASSWORD"
        val expected = GeneratedPasswordResponse("A_RANDOM_PASSWORD")

        mokMvc.perform(
            post("/api/password")
                .principal(m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.GENERATE_PASSWORD.content)))
        )
            .andExpect(content().json(objectMapper.writeValueAsString(expected)))
            .andExpect(status().isOk)

        verify { passwordGenerator.generate() }
    }

    @Test
    fun `when a new random password is generated with admin full access scope`() {
        every { passwordGenerator.generate() } returns "A_RANDOM_PASSWORD"

        mokMvc.perform(
            post("/api/password")
                .principal(m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.ADMIN_FULL_ACCESS.content)))
        )
            .andExpect(status().isOk)

        verify { passwordGenerator.generate() }
    }

    @Test
    fun `generating a new random password fails for insufficient scope`() {
        mokMvc.perform(
            post("/api/password")
                .principal(m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content)))
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { passwordGenerator.generate() }
    }

    @Test
    fun `generating a new random password is unauthorized for a session user`() {
        mokMvc.perform(
            post("/api/password")
                .principal(principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN)))
        )
            .andExpect(status().isUnauthorized)

        verify(exactly = 0) { passwordGenerator.generate() }
    }
}
