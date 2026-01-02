package com.vauthenticator.server.web

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.web.cors.CorsConfigurationResolver
import com.vauthenticator.server.web.cors.DynamicCorsConfigurationSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfigurationSource


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

}