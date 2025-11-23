package com.vauthenticator.server.role.adapter

import com.vauthenticator.server.role.domain.ProtectedRoleFromDeletionException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractRoleRepositoryTest {
    private lateinit var uut: RoleRepository

    abstract fun initRoleRepository(): RoleRepository
    abstract fun resetDatabase()

    @BeforeEach
    fun setUp() {
        uut = initRoleRepository()
        resetDatabase()
    }


    @Test
    fun findAllRoles() {
        val actual = uut.findAll()
        val expected: List<Role> = listOf(Role("a_role", "A_ROLE"))

        assertEquals(expected, actual)
    }

    @Test
    fun `save a new role`() {
        uut.save(Role("another_role", "ANOTHER_ROLE"))
        val expected = uut.findAll().toSet()
        val actual = listOf(Role("a_role", "A_ROLE"), Role("another_role", "ANOTHER_ROLE")).toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun `save a new role again`() {
        uut.save(Role("another_role", "ANOTHER_ROLE"))
        uut.save(Role("another_role", "ANOTHER_ROLE AGAIN"))
        val expected = uut.findAll().toSet()
        val actual = listOf(Role("a_role", "A_ROLE"), Role("another_role", "ANOTHER_ROLE AGAIN")).toSet()

        assertEquals(expected, actual)
    }

    @Test
    fun `delete a role`() {
        uut.delete("a_role")
        val roles = uut.findAll()

        assertTrue { roles.isEmpty() }
    }

    @Test
    fun `when default role is attempted to be deleted`() {
        assertThrows(ProtectedRoleFromDeletionException::class.java) { uut.delete("ROLE_USER") }
    }
}