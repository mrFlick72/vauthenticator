package com.vauthenticator.server.role.adapter.jdbc

import com.vauthenticator.server.role.domain.ProtectedRoleFromDeletionException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.role.domain.RoleRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.annotation.Transactional

private const val SAVE_QUERY: String =
    "INSERT INTO Role (name,description) VALUES (?,?) ON CONFLICT(name) DO UPDATE SET description=?"
private const val FIND_ALL_QUERY: String = "SELECT name,description FROM Role"
private const val DELETE_QUERY: String = "DELETE FROM Role WHERE name=?"

@Transactional
class JdbcRoleRepository(
    val jdbcTemplate: JdbcTemplate,
    val protectedRoleFromDeletion: List<String>
) : RoleRepository {

    @Transactional(readOnly = true)
    override fun findAll(): List<Role> =
        jdbcTemplate.query(FIND_ALL_QUERY) { rs, _ -> Role(rs.getString("name"), rs.getString("description")) }


    override fun save(role: Role) {
        jdbcTemplate.update(SAVE_QUERY, role.name, role.description, role.description)
    }

    override fun delete(roleName: String) {
        if (protectedRoleFromDeletion.contains(roleName)) {
            throw ProtectedRoleFromDeletionException("the role $roleName is not allowed to be deleted")
        } else {
            jdbcTemplate.update(DELETE_QUERY, roleName)
        }
    }

}
