package com.vauthenticator.server.role.domain

interface RoleRepository {

    fun findAll(): List<Role>
    fun save(role: Role)
    fun delete(role: String)

}

