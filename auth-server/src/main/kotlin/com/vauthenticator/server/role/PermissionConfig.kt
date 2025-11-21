package com.vauthenticator.server.role

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.cache.RedisCacheOperation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.role.domain.RoleCacheContentConverter
import com.vauthenticator.server.role.adapter.CachedRoleRepository
import com.vauthenticator.server.role.adapter.dynamodb.DynamoDbRoleRepository
import com.vauthenticator.server.role.adapter.jdbc.JdbcGroupRepository
import com.vauthenticator.server.role.adapter.jdbc.JdbcRoleRepository
import com.vauthenticator.server.role.adapter.token.RoleTokenEnhancer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class PermissionConfig {

    @Bean("groupRepository")
    @Profile("database")
    fun jdbcGroupRepository(
        jdbcClient: JdbcClient,
    ) = JdbcGroupRepository(jdbcClient)

    @Bean("roleRepository")
    @Profile("database")
    fun jdbcRoleRepository(
        jdbcTemplate: JdbcTemplate,
        @Value("\${vauthenticator.dynamo-db.role.protected-from-delete}") protectedRoleFromDeletion: List<String>
    ) = JdbcRoleRepository(jdbcTemplate, protectedRoleFromDeletion)

    @Bean("roleRepository")
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.role.cache.enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    @Profile("dynamo")
    fun dynamoDbRoleRepository(
        dynamoDbClient: DynamoDbClient,
        @Value("\${vauthenticator.dynamo-db.role.table-name}") roleTableName: String,
        @Value("\${vauthenticator.dynamo-db.role.protected-from-delete}") protectedRoleFromDeletion: List<String>
    ) = DynamoDbRoleRepository(protectedRoleFromDeletion, dynamoDbClient, roleTableName)


    @Bean("roleRepository")
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.role.cache.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile("dynamo")
    fun cachedDynamoDbRoleRepository(
        mapper: ObjectMapper,
        roleCacheOperation: CacheOperation<String, String>,
        dynamoDbClient: DynamoDbClient,
        @Value("\${vauthenticator.dynamo-db.role.table-name}") roleTableName: String,
        @Value("\${vauthenticator.dynamo-db.role.protected-from-delete}") protectedRoleFromDeletion: List<String>
    ) = CachedRoleRepository(
        RoleCacheContentConverter(mapper),
        roleCacheOperation,
        DynamoDbRoleRepository(protectedRoleFromDeletion, dynamoDbClient, roleTableName)
    )

    @Bean
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.role.cache.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile("dynamo")
    fun roleCacheOperation(
        redisTemplate: RedisTemplate<*, *>,
        @Value("\${vauthenticator.dynamo-db.role.cache.ttl}") ttl: Duration,
        @Value("\${vauthenticator.dynamo-db.role.cache.name}") cacheRegionName: String,
    ) = RedisCacheOperation<String, String>(
        cacheName = cacheRegionName,
        ttl = ttl,
        redisTemplate = redisTemplate as RedisTemplate<String, String>
    )

    @Bean
    fun permissionValidator(clientApplicationRepository: ClientApplicationRepository) =
        PermissionValidator(clientApplicationRepository)
}