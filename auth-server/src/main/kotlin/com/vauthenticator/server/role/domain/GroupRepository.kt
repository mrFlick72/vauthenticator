package com.vauthenticator.server.role.domain

interface GroupRepository {

    fun loadFor(groupName: String): GroupWitRoles?
    fun findAll(): List<Group>
    fun save(group: Group)
    fun delete(groupName: String)

    fun roleAssociation(groupName: String, vararg roleNames : String)
    fun roleDeAssociation(groupName: String, vararg roleNames : String)
}