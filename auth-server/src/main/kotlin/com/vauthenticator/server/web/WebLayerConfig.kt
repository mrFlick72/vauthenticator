package com.vauthenticator.server.web

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.web.cors.CorsConfigurationResolver
import com.vauthenticator.server.web.cors.DynamicCorsConfigurationSource
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfigurationSource
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.SerializationFeature


@Configuration(proxyBeanMethods = false)
class WebLayerConfig {

    @Bean
    fun corsConfigurationResolver(allowedOriginRepository: AllowedOriginRepository): CorsConfigurationResolver =
        CorsConfigurationResolver(allowedOriginRepository)

    @Bean
    fun springCurrentHttpServletRequestService() =
        SpringCurrentHttpServletRequestService()

    @Bean
    fun corsConfigurationSource(corsConfigurationResolver: CorsConfigurationResolver): CorsConfigurationSource =
        DynamicCorsConfigurationSource(corsConfigurationResolver)

    @Bean
    fun jsonCustomizer(): JsonMapperBuilderCustomizer =
        JsonMapperBuilderCustomizer { builder ->
            builder.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            builder.disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        }
}