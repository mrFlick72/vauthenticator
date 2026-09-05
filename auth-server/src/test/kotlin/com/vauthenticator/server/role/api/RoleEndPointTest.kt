package com.vauthenticator.server.role.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

private const val A_ROLE_DESCRIPTION = "A Role description"

@ExtendWith(MockKExtension::class)
internal class RoleEndPointTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var roleRepository: RoleRepository

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(RoleEndPoint(roleRepository, PermissionValidator(clientApplicationRepository)))
            .setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `find al roles`() {
        val roles = listOf(
            Role("a_role1", A_ROLE_DESCRIPTION),
            Role("a_role2", A_ROLE_DESCRIPTION),
            Role("a_role3", A_ROLE_DESCRIPTION)
        )
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.READ_ROLE.content))

        every { roleRepository.findAll() } returns roles

        mokMvc.perform(
            get("/api/roles")
                .accept(MediaType.APPLICATION_JSON)
                .principal(jwtAuthenticationToken)
        )
            .andExpect(content().string(objectMapper.writeValueAsString(roles)))

        verify { roleRepository.findAll() }
    }

    @Test
    fun `find al roles fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            get("/api/roles")
                .accept(MediaType.APPLICATION_JSON)
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { roleRepository.findAll() }
    }

    @Test
    fun `save a new role`() {
        val role = Role("a_role", A_ROLE_DESCRIPTION)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_ROLE.content))
        every { roleRepository.save(role) } just runs

        mokMvc.perform(
            put("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(role))
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)

        verify { roleRepository.save(role) }
    }

    @Test
    fun `save a new role fails for insufficient scope`() {
        val role = Role("a_role", A_ROLE_DESCRIPTION)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            put("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(role))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { roleRepository.save(role) }
    }

    @Test
    fun `delete a new role`() {
        val role = "a_role"
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.DELETE_ROLE.content))
        every { roleRepository.delete(role) } just runs

        mokMvc.perform(
            delete("/api/roles/a_role")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)

        verify { roleRepository.delete(role) }
    }

    @Test
    fun `delete a new role fails for insufficient scope`() {
        val role = "a_role"
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            delete("/api/roles/a_role")
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { roleRepository.delete(role) }
    }
}
