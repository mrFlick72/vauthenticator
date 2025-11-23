package com.vauthenticator.management.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.management.oauth2.WellKnownEndPointAvailableScopeRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration(proxyBeanMethods = false)
class ScopesConfig {

    @Bean
    fun availableScopeRepository(
        objectMapper: ObjectMapper,
        @Value("\${vauthenticator.backChannelHost}") vauthenticatoBaseUrl: String
    ) = WellKnownEndPointAvailableScopeRepository(objectMapper, vauthenticatoBaseUrl, RestTemplate())
}