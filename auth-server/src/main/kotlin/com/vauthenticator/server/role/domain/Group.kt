package com.vauthenticator.server.role.domain

data class Group(val name: String, val description: String)
data class GroupWitRoles(val group: Group, val roles: List<Role>)