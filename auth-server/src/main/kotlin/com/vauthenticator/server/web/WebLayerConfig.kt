package com.vauthenticator.server.web

import com.vauthenticator.server.config.FRONT_CHANNEL_LOG_OUT_URL
import com.vauthenticator.server.config.LOG_IN_URL_PAGE
import com.vauthenticator.server.config.LOG_OUR_URL
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.web.cors.AuthServerCorsFilter
import com.vauthenticator.server.web.cors.CorsConfigurationResolver
import com.vauthenticator.server.web.cors.DynamicCorsConfigurationSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.web.cors.CorsConfigurationSource
import kotlin.collections.listOf

@Configuration(proxyBeanMethods = false)
class WebLayerConfig {


    @Bean
    @Order(Integer.MIN_VALUE)
    fun authServerCorsFilter(
        corsConfigurationResolver: CorsConfigurationResolver,
        providerSettings: AuthorizationServerSettings
    ) =
        AuthServerCorsFilter(
            corsConfigurationResolver,
            listOf(
                providerSettings.jwkSetEndpoint,
                providerSettings.authorizationEndpoint,
                providerSettings.deviceAuthorizationEndpoint,
                providerSettings.tokenEndpoint,
                providerSettings.oidcUserInfoEndpoint,
                providerSettings.oidcLogoutEndpoint,
                providerSettings.tokenRevocationEndpoint,
                providerSettings.tokenIntrospectionEndpoint,
                LOG_IN_URL_PAGE,
                FRONT_CHANNEL_LOG_OUT_URL,
                LOG_OUR_URL
            )
        )

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