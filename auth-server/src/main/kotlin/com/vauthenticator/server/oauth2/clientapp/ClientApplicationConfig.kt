package com.vauthenticator.server.oauth2.clientapp

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.cache.RedisCacheOperation
import com.vauthenticator.server.oauth2.clientapp.adapter.cache.CachedClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.cache.ClientApplicationCacheContentConverter
import com.vauthenticator.server.oauth2.clientapp.adapter.dynamodb.DynamoDbClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.inmemory.InMemoryAllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.jdbc.JdbcClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.redis.RedisAllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ReadClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.StoreClientApplication
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class ClientApplicationConfig {

    @Bean
    fun allowedOriginRepository(redisTemplate: RedisTemplate<String, String>): AllowedOriginRepository =
        RedisAllowedOriginRepository(redisTemplate)

    @Bean("clientApplicationRepository")
    @Profile("database")
    fun jdbcClientApplicationRepository(
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
        objectMapper: ObjectMapper,
        allowedOriginRepository: AllowedOriginRepository
    ): ClientApplicationRepository =
        JdbcClientApplicationRepository(namedParameterJdbcTemplate, objectMapper, allowedOriginRepository)

    @Bean("clientApplicationRepository")
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.client-application.cache.enabled"],
        havingValue = "false",
        matchIfMissing = true
    )
    @Profile("dynamo")
    fun dynamoDbClientApplicationRepository(
        dynamoDbClient: DynamoDbClient,
        @Value("\${vauthenticator.dynamo-db.client-application.table-name}") clientAppTableName: String,
        allowedOriginRepository: AllowedOriginRepository
    ): ClientApplicationRepository =
        DynamoDbClientApplicationRepository(dynamoDbClient, clientAppTableName, allowedOriginRepository)

    @Bean("clientApplicationRepository")
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.client-application.cache.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile("dynamo")
    fun cachedClientApplicationRepository(
        dynamoDbClient: DynamoDbClient,
        clientApplicationCacheOperation: CacheOperation<String, String>,
        objectMapper: ObjectMapper,
        @Value("\${vauthenticator.dynamo-db.client-application.table-name}") clientAppTableName: String,
        allowedOriginRepository: AllowedOriginRepository
    ) = CachedClientApplicationRepository(
        ClientApplicationCacheContentConverter(objectMapper),
        clientApplicationCacheOperation,
        DynamoDbClientApplicationRepository(dynamoDbClient, clientAppTableName, allowedOriginRepository)
    )

    @Bean
    @ConditionalOnProperty(
        name = ["vauthenticator.dynamo-db.client-application.cache.enabled"],
        havingValue = "true",
        matchIfMissing = false
    )
    @Profile("dynamo")
    fun clientApplicationCacheOperation(
        redisTemplate: RedisTemplate<*, *>,
        @Value("\${vauthenticator.dynamo-db.client-application.cache.ttl}") ttl: Duration,
        @Value("\${vauthenticator.dynamo-db.client-application.cache.name}") cacheRegionName: String,
    ) = RedisCacheOperation<String, String>(
        cacheName = cacheRegionName,
        ttl = ttl,
        redisTemplate = redisTemplate as RedisTemplate<String, String>
    )

    @Bean
    fun readClientApplication(clientApplicationRepository: ClientApplicationRepository) =
        ReadClientApplication(clientApplicationRepository)

    @Bean
    fun storeClientApplication(
        clientApplicationRepository: ClientApplicationRepository,
        passwordEncoder: VAuthenticatorPasswordEncoder
    ) =
        StoreClientApplication(clientApplicationRepository, passwordEncoder)

}