package com.vauthenticator.server.role

import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.role.adapter.CachedRoleRepository
import com.vauthenticator.server.role.adapter.cache.AccountCacheEvictingGroupRepository
import com.vauthenticator.server.role.adapter.cache.AccountCacheEvictingRoleRepository
import com.vauthenticator.server.role.adapter.jdbc.JdbcGroupRepository
import com.vauthenticator.server.role.adapter.jdbc.JdbcRoleRepository
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.role.domain.RoleCacheContentConverter
import com.vauthenticator.server.role.domain.RoleRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.JdbcClient
import tools.jackson.databind.ObjectMapper

@Configuration(proxyBeanMethods = false)
class RoleRepositoryConfig {

    @Bean("groupRepository")
    fun groupRepository(
        jdbcClient: JdbcClient,
        @Qualifier("accountCacheOperation") accountCacheOperation: ObjectProvider<CacheOperation<String, String>>
    ): GroupRepository {
        val jdbcGroupRepository = JdbcGroupRepository(jdbcClient)
        return accountCacheOperation.ifAvailable
            ?.let { AccountCacheEvictingGroupRepository(jdbcGroupRepository, it) }
            ?: jdbcGroupRepository
    }

    @Bean("roleRepository")
    fun roleRepository(
        jdbcTemplate: JdbcTemplate,
        objectMapper: ObjectMapper,
        @Value("\${vauthenticator.role.protected-from-delete}") protectedRoleFromDeletion: List<String>,
        @Qualifier("roleCacheOperation") roleCacheOperation: ObjectProvider<CacheOperation<String, String>>,
        @Qualifier("accountCacheOperation") accountCacheOperation: ObjectProvider<CacheOperation<String, String>>
    ): RoleRepository {
        val jdbcRoleRepository = JdbcRoleRepository(jdbcTemplate, protectedRoleFromDeletion)
        val roleRepository = roleCacheOperation.ifAvailable
            ?.let { CachedRoleRepository(RoleCacheContentConverter(objectMapper), it, jdbcRoleRepository) }
            ?: jdbcRoleRepository
        return accountCacheOperation.ifAvailable
            ?.let { AccountCacheEvictingRoleRepository(roleRepository, it) }
            ?: roleRepository
    }
}

@Configuration(proxyBeanMethods = false)
class PermissionConfig {

    @Bean
    fun permissionValidator(clientApplicationRepository: ClientApplicationRepository) =
        PermissionValidator(clientApplicationRepository)
}
