package com.vauthenticator.server.cache

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import java.time.Duration

/**
 * The opt-in Redis cache regions. Every write below a cached repository must evict from its region (ADR-0012).
 */
@Configuration(proxyBeanMethods = false)
class CacheConfig {

    @Bean
    @ConditionalOnProperty(name = ["vauthenticator.cache.account.enabled"], havingValue = "true")
    fun accountCacheOperation(
        redisTemplate: RedisTemplate<*, *>,
        @Value("\${vauthenticator.cache.account.ttl}") ttl: Duration,
        @Value("\${vauthenticator.cache.account.name}") cacheRegionName: String,
    ) = cacheOperationFor(cacheRegionName, ttl, redisTemplate)

    @Bean
    @ConditionalOnProperty(name = ["vauthenticator.cache.role.enabled"], havingValue = "true")
    fun roleCacheOperation(
        redisTemplate: RedisTemplate<*, *>,
        @Value("\${vauthenticator.cache.role.ttl}") ttl: Duration,
        @Value("\${vauthenticator.cache.role.name}") cacheRegionName: String,
    ) = cacheOperationFor(cacheRegionName, ttl, redisTemplate)

    @Bean
    @ConditionalOnProperty(name = ["vauthenticator.cache.client-application.enabled"], havingValue = "true")
    fun clientApplicationCacheOperation(
        redisTemplate: RedisTemplate<*, *>,
        @Value("\${vauthenticator.cache.client-application.ttl}") ttl: Duration,
        @Value("\${vauthenticator.cache.client-application.name}") cacheRegionName: String,
    ) = cacheOperationFor(cacheRegionName, ttl, redisTemplate)

    private fun cacheOperationFor(
        cacheRegionName: String,
        ttl: Duration,
        redisTemplate: RedisTemplate<*, *>
    ): CacheOperation<String, String> = RedisCacheOperation(
        cacheName = cacheRegionName,
        ttl = ttl,
        redisTemplate = redisTemplate as RedisTemplate<String, String>
    )
}
