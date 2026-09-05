package com.vauthenticator.server.role.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.Group
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.PermissionValidator
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

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(GroupEndPoint(groupRepository, PermissionValidator(clientApplicationRepository)))
            .setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `find al groups`() {
        val groups = listOf(
            Group("a_group_1", A_GROUP_DESCRIPTION),
            Group("a_group_2", A_GROUP_DESCRIPTION),
            Group("a_group_3", A_GROUP_DESCRIPTION)
        )
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.READ_GROUP.content))
        every { groupRepository.findAll() } returns groups

        mokMvc.perform(
            get("/api/groups")
                .accept(MediaType.APPLICATION_JSON)
                .principal(jwtAuthenticationToken)
        )
            .andExpect(content().string(objectMapper.writeValueAsString(groups)))

        verify { groupRepository.findAll() }
    }

    @Test
    fun `find al groups fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            get("/api/groups")
                .accept(MediaType.APPLICATION_JSON)
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { groupRepository.findAll() }
    }

    @Test
    fun `save a new group`() {
        val group = Group("A_GROUP", "A_GROUP_DESCRIPTION")
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_GROUP.content))
        every { groupRepository.save(group) } just runs

        mokMvc.perform(
            put("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group))
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)

        verify { groupRepository.save(group) }
    }

    @Test
    fun `save a new group fails for insufficient scope`() {
        val group = Group("A_GROUP", "A_GROUP_DESCRIPTION")
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            put("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(group))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { groupRepository.save(group) }
    }

    @Test
    fun `delete a new group`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.DELETE_GROUP.content))
        every { groupRepository.delete("a_group_1") } just runs

        mokMvc.perform(
            delete("/api/groups/a_group_1")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)

        verify { groupRepository.delete("a_group_1") }
    }

    @Test
    fun `delete a new group fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            delete("/api/groups/a_group_1")
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { groupRepository.delete("a_group_1") }
    }

    @Test
    fun `associate some roles to a group`() {
        val roleToGroupAssociationRepresentation = RoleToGroupAssociationRepresentation(
            toBeAssociated = listOf("a_role_1", "a_role_2", "a_role_3"),
            toBeDeAssociated = listOf("a_role_4", "a_role_5")
        )
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_GROUP.content))
        every { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") } just runs
        every { groupRepository.roleDeAssociation("a_group_1", "a_role_4", "a_role_5") } just runs

        mokMvc.perform(
            put("/api/groups/a_group_1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleToGroupAssociationRepresentation))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isNoContent)


        verify { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") }
        verify { groupRepository.roleDeAssociation("a_group_1", "a_role_4", "a_role_5") }

    }

    @Test
    fun `associate some roles to a group fails for insufficient scope`() {
        val roleToGroupAssociationRepresentation = RoleToGroupAssociationRepresentation(
            toBeAssociated = listOf("a_role_1", "a_role_2", "a_role_3"),
            toBeDeAssociated = listOf("a_role_4", "a_role_5")
        )
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            put("/api/groups/a_group_1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleToGroupAssociationRepresentation))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") }
        verify(exactly = 0) { groupRepository.roleDeAssociation("a_group_1", "a_role_4", "a_role_5") }
    }

    @Test
    fun `associate some roles to a group fails due to one role is added and removed in the same reques`() {
        val roleToGroupAssociationRepresentation = RoleToGroupAssociationRepresentation(
            toBeAssociated = listOf("a_role_1", "a_role_2", "a_role_3"),
            toBeDeAssociated = listOf("a_role_1", "a_role_4")
        )
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_GROUP.content))

        mokMvc.perform(
            put("/api/groups/a_group_1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roleToGroupAssociationRepresentation))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isBadRequest)


        verify(exactly = 0) { groupRepository.roleAssociation("a_group_1", "a_role_1", "a_role_2", "a_role_3") }
        verify(exactly = 0) { groupRepository.roleDeAssociation("a_group_1", "a_role_4") }

    }

}
