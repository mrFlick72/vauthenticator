package com.vauthenticator.server.oauth2.clientapp

import com.vauthenticator.server.oauth2.clientapp.adapter.jdbc.JdbcClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.adapter.redis.RedisAllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ReadClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.StoreClientApplication
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import tools.jackson.databind.ObjectMapper

@Configuration(proxyBeanMethods = false)
class ClientApplicationConfig {

    @Bean
    fun allowedOriginRepository(redisTemplate: RedisTemplate<String, String>): AllowedOriginRepository =
        RedisAllowedOriginRepository(redisTemplate)

    @Bean("clientApplicationRepository")
    fun jdbcClientApplicationRepository(
        namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
        objectMapper: ObjectMapper,
        allowedOriginRepository: AllowedOriginRepository
    ): ClientApplicationRepository =
        JdbcClientApplicationRepository(namedParameterJdbcTemplate, objectMapper, allowedOriginRepository)

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