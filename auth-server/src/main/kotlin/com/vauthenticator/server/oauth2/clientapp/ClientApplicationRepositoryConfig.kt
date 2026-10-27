package com.vauthenticator.server.oauth2.clientapp

import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.oauth2.clientapp.adapter.cache.CachedClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.cache.ClientApplicationCacheContentConverter
import com.vauthenticator.server.oauth2.clientapp.adapter.jdbc.JdbcClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.redis.RedisAllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import tools.jackson.databind.ObjectMapper

@Configuration(proxyBeanMethods = false)
class ClientApplicationRepositoryConfig {

    @Bean
    fun allowedOriginRepository(redisTemplate: RedisTemplate<String, String>): AllowedOriginRepository =
        RedisAllowedOriginRepository(redisTemplate)

    @Bean("clientApplicationRepository")
    fun clientApplicationRepository(
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
        objectMapper: ObjectMapper,
        allowedOriginRepository: AllowedOriginRepository,
        @Qualifier("clientApplicationCacheOperation") clientApplicationCacheOperation: ObjectProvider<CacheOperation<String, String>>
    ): ClientApplicationRepository {
        val jdbcClientApplicationRepository =
            JdbcClientApplicationRepository(namedParameterJdbcTemplate, objectMapper, allowedOriginRepository)
        return clientApplicationCacheOperation.ifAvailable
            ?.let {
                CachedClientApplicationRepository(
                    ClientApplicationCacheContentConverter(objectMapper),
                    it,
                    jdbcClientApplicationRepository
                )
            }
            ?: jdbcClientApplicationRepository
    }
}
