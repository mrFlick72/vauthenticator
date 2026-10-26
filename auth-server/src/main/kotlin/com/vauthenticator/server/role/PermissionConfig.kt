package com.vauthenticator.server.role

import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.role.adapter.jdbc.JdbcGroupRepository
import com.vauthenticator.server.role.adapter.jdbc.JdbcRoleRepository
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.JdbcClient

@Configuration(proxyBeanMethods = false)
class PermissionConfig {

    @Bean("groupRepository")
    fun jdbcGroupRepository(
        jdbcClient: JdbcClient,
    ) = JdbcGroupRepository(jdbcClient)

    @Bean("roleRepository")
    fun jdbcRoleRepository(
        jdbcTemplate: JdbcTemplate,
        @Value("\${vauthenticator.role.protected-from-delete}") protectedRoleFromDeletion: List<String>
    ) = JdbcRoleRepository(jdbcTemplate, protectedRoleFromDeletion)

    @Bean
    fun permissionValidator(clientApplicationRepository: ClientApplicationRepository) =
        PermissionValidator(clientApplicationRepository)
}