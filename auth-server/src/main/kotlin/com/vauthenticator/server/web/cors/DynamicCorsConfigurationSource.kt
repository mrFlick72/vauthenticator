package com.vauthenticator.server.web.cors

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource

class DynamicCorsConfigurationSource(private val corsConfigurationResolver: CorsConfigurationResolver) :
    CorsConfigurationSource {
    override fun getCorsConfiguration(request: HttpServletRequest): CorsConfiguration {
        val corsConfiguration = CorsConfiguration()

        val corsConfigurationTemplate = corsConfigurationResolver.configurationFor(request)
        corsConfiguration.allowedOrigins = listOf(corsConfigurationTemplate.allowedOrigin)
        corsConfiguration.maxAge = corsConfigurationTemplate.maxAge
        corsConfiguration.allowedMethods = corsConfigurationTemplate.allowedMethods
        corsConfiguration.allowCredentials = corsConfigurationTemplate.allowCredentials

        return corsConfiguration
    }

}