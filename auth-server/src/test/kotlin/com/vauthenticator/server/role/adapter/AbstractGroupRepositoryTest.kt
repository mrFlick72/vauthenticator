package com.vauthenticator.server.role.adapter

import com.vauthenticator.server.role.domain.*
import com.vauthenticator.server.support.JdbcUtils.jdbcClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.test.assertTrue

private const val A_GROUP = "a_group"
private const val ANOTHER_GROUP = "another_group"

abstract class AbstractGroupRepositoryTest {

    lateinit var uut: GroupRepository
    lateinit var roleRepository: RoleRepository

    abstract fun initGroupRepository(): GroupRepository
    abstract fun initRoleRepository(): RoleRepository
    abstract fun resetDatabase()

    @BeforeEach
    fun setUp() {
        uut = initGroupRepository()
        roleRepository = initRoleRepository()
        resetDatabase()
    }

    @Test
    fun `when save a new group`() {
        uut.save(Group(A_GROUP, "a description"))
        val actual = uut.loadFor(A_GROUP)
        val expected = GroupWitRoles(group = Group(A_GROUP, "a description"), roles = emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `when save a new group multiple times`() {
        uut.save(Group(A_GROUP, "a description"))
        uut.save(Group(A_GROUP, "a description"))
        uut.save(Group(A_GROUP, "a new description"))
        val actual = uut.loadFor(A_GROUP)
        val expected = GroupWitRoles(group = Group(A_GROUP, "a new description"), roles = emptyList())

        assertEquals(expected, actual)
    }

    @Test
    fun `when add roles to a new group`() {
        uut.save(Group(A_GROUP, "a description"))
        uut.roleAssociation(A_GROUP, "a_role_name", "another_role_name")
        uut.roleAssociation(A_GROUP, "a_role", "a_role_name", "another_role_name")
        val actual = uut.loadFor(A_GROUP)

        val expected = GroupWitRoles(
            group = Group(A_GROUP, "a description"),
            roles = listOf(
                Role("a_role", "A_ROLE"),
                Role("a_role_name", "a_role_description"),
                Role("another_role_name", "another_role_description"),
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when remove roles to a new group`() {
        uut.save(Group(A_GROUP, "a description"))
        uut.roleAssociation(A_GROUP, "a_role_name", "another_role_name")
        uut.roleDeAssociation(A_GROUP, "another_role_name")
        uut.roleDeAssociation(A_GROUP, "another_role_name")
        uut.roleDeAssociation(A_GROUP, "another_role_name")

        val actual = uut.loadFor(A_GROUP)

        val expected = GroupWitRoles(
            group = Group(A_GROUP, "a description"),
            roles = listOf(
                Role("a_role_name", "a_role_description"),
            )
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when delete a new group`() {
        uut.save(Group(A_GROUP, "a description"))
        uut.roleAssociation(A_GROUP, "a_role_name", "another_role_name")
        uut.delete(A_GROUP)

        val actual = uut.loadFor(A_GROUP)
        val listOfRows = jdbcClient.sql("SELECT * FROM GROUPS_ROLE WHERE group_name=:groupName;")
            .param("groupName", A_GROUP)
            .query()
            .listOfRows()

        assertNull(actual)
        assertTrue { listOfRows.isEmpty() }
    }

    @Test
    fun `when get all groups`() {
        uut.save(Group(A_GROUP, "a description"))
        uut.save(Group(ANOTHER_GROUP, "a description"))

        val actual = uut.findAll()
        assertEquals(
            listOf(
                Group(A_GROUP, "a description"),
                Group(ANOTHER_GROUP, "a description")
            ), actual
        )
    }

    @Test
    fun `when an associated role is deleted`() {
        uut.save(Group(A_GROUP, "a description"))
        uut.roleAssociation(A_GROUP, "a_role_name", "another_role_name")

        roleRepository.delete("a_role_name")

        val actual = uut.loadFor(A_GROUP)

        assertEquals(actual?.roles, listOf(Role("another_role_name", "another_role_description")))
    }
}