package com.vauthenticator.server.role.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.role.domain.Group
import com.vauthenticator.server.role.domain.GroupRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

private const val A_GROUP_DESCRIPTION = "A Group description"


@ExtendWith(MockKExtension::class)
class GroupEndPointTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var groupRepository: GroupRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(GroupEndPoint(groupRepository)).build()
    }

    @Test
    fun `find al groups`() {
        val groups = listOf(
            Group("a_group_1", A_GROUP_DESCRIPTION),
            Group("a_group_2", A_GROUP_DESCRIPTION),
            Group("a_group_3", A_GROUP_DESCRIPTION)
        )
        every { groupRepository.findAll() } returns groups

        mokMvc.perform(
            get("/api/groups")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(content().string(objectMapper.writeValueAsString(groups)))

        verify { groupRepository.findAll() }
    }

    @Test
    fun `save a new group`() {
        val group = Group("A_GROUP", "A_GROUP_DESCRIPTION")
        every { groupRepository.save(group) } just runs

        mokMvc.perform(
            put("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group))
        )
            .andExpect(status().isNoContent)

        verify { groupRepository.save(group) }
    }

    @Test
    fun `delete a new group`() {
        every { groupRepository.delete("a_group_1") } just runs

        mokMvc.perform(delete("/api/groups/a_group_1"))
            .andExpect(status().isNoContent)

        verify { groupRepository.delete("a_group_1") }
    }

    @Test
    fun `associate some roles to a group`() {
        val roleToGroupAssociationRepresentation = RoleToGroupAssociationRepresentation(
            toBeAssociated = listOf("a_role_1", "a_role_2", "a_role_3"),
            toBeDeAssociated = listOf("a_role_4", "a_role_5")
        )
        every { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") } just runs
        every { groupRepository.roleDeAssociation("a_group_1", "a_role_4", "a_role_5") } just runs

        mokMvc.perform(
            put("/api/groups/a_group_1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleToGroupAssociationRepresentation))
        ).andExpect(status().isNoContent)


        verify { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") }
        verify { groupRepository.roleDeAssociation("a_group_1", "a_role_4", "a_role_5") }

    }

    @Test
    fun `associate some roles to a group fails due to one role is added and removed in the same reques`() {
        val roleToGroupAssociationRepresentation = RoleToGroupAssociationRepresentation(
            toBeAssociated = listOf("a_role_1", "a_role_2", "a_role_3"),
            toBeDeAssociated = listOf("a_role_1", "a_role_4")
        )

        mokMvc.perform(
            put("/api/groups/a_group_1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleToGroupAssociationRepresentation))
        ).andExpect(status().isBadRequest)


        verify(exactly = 0) { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") }
        verify(exactly = 0) { groupRepository.roleDeAssociation("a_group_1", "a_role_4") }

    }

}