package com.vauthenticator.server.role.adapter.jdbc

import com.vauthenticator.server.role.domain.Group
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.GroupWitRoles
import com.vauthenticator.server.role.domain.Role
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional


private const val FIND_ONE_QUERY = "SELECT * FROM GROUPS WHERE name=:groupName"

private const val FIND_ALL_QUERY = "SELECT * FROM GROUPS"

private const val SAVE_ONE_GROUP = "INSERT INTO GROUPS (name,description) VALUES (:groupName, :description) ON CONFLICT(name) DO UPDATE SET description=:description;"

private const val DELETE_ONE_GROUP = "DELETE FROM GROUPS WHERE name=:groupName;"

private const val GROUP_ROLE_ASSOCIATION = "INSERT INTO GROUPS_ROLE (group_name,role_name) VALUES (:groupName, :roleName) ON CONFLICT(group_name,role_name) DO UPDATE SET group_name=:groupName, role_name=:roleName;"

private const val GROUP_ROLE_DE_ASSOCIATION = "DELETE FROM GROUPS_ROLE WHERE group_name=:groupName AND role_name=:roleName;"

private const val GET_ROLE_BY_GROUP =
    "SELECT * FROM GROUPS_ROLE as group_role join ROLE as role ON group_role.role_name=role.name WHERE group_role.group_name=:groupName;"

@Transactional
class JdbcGroupRepository(val jdbcClient: JdbcClient) : GroupRepository {

    @Transactional(readOnly = true)
    override fun loadFor(groupName: String): GroupWitRoles? {
        return jdbcClient.sql(FIND_ONE_QUERY)
            .param("groupName", groupName)
            .query { rs, _ ->
                GroupWitRoles(
                    group = Group(rs.getString("name"), rs.getString("description")),
                    roles = roleAssociationFor(groupName)
                )
            }
            .list()
            .firstOrNull()
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Group> {
        return jdbcClient.sql(FIND_ALL_QUERY)
            .query { rs, _ -> Group(rs.getString("name"), rs.getString("description")) }
            .list()
    }

    override fun save(group: Group) {
        jdbcClient.sql(SAVE_ONE_GROUP)
            .param("groupName", group.name)
            .param("description", group.description)
            .update()
    }

    override fun delete(groupName: String) {
        jdbcClient.sql(DELETE_ONE_GROUP)
            .param("groupName", groupName)
            .update()
    }

    override fun roleAssociation(groupName: String, vararg roleNames: String) {
        arrayOf(*roleNames)
            .forEach { roleName ->
                jdbcClient.sql(GROUP_ROLE_ASSOCIATION)
                    .param("groupName", groupName)
                    .param("roleName", roleName)
                    .update()
            }

    }

    override fun roleDeAssociation(groupName: String, vararg roleNames: String) {
        arrayOf(*roleNames)
            .forEach { roleName ->
                jdbcClient.sql(GROUP_ROLE_DE_ASSOCIATION)
                    .param("groupName", groupName)
                    .param("roleName", roleName)
                    .update()
            }
    }

    private fun roleAssociationFor(groupName: String): List<Role> {
        return jdbcClient.sql(GET_ROLE_BY_GROUP)
            .param("groupName", groupName)
            .query { rs, _ -> Role(rs.getString("name"), rs.getString("description")) }
            .list()
    }
}
