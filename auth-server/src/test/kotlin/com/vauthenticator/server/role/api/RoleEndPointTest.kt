package com.vauthenticator.server.role.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
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

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(RoleEndPoint(roleRepository)).build()
    }

    @Test
    fun `find al roles`() {
        val roles = listOf(
            Role("a_role1", A_ROLE_DESCRIPTION),
            Role("a_role2", A_ROLE_DESCRIPTION),
            Role("a_role3", A_ROLE_DESCRIPTION)
        )

        every { roleRepository.findAll() } returns roles

        mokMvc.perform(
            get("/api/roles")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(content().string(objectMapper.writeValueAsString(roles)))

        verify { roleRepository.findAll() }
    }

    @Test
    fun `save a new role`() {
        val role = Role("a_role", A_ROLE_DESCRIPTION)
        every { roleRepository.save(role) } just runs

        mokMvc.perform(
            put("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(role))
        )
            .andExpect(status().isNoContent)

        verify { roleRepository.save(role) }
    }

    @Test
    fun `delete a new role`() {
        val role = "a_role"
        every { roleRepository.delete(role) } just runs

        mokMvc.perform(delete("/api/roles/a_role"))
            .andExpect(status().isNoContent)

        verify { roleRepository.delete(role) }
    }
}