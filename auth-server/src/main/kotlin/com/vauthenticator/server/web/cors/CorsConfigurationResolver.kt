package com.vauthenticator.server.web.cors

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import jakarta.servlet.http.HttpServletRequest

class CorsConfigurationResolver(private val allowedOriginRepository: AllowedOriginRepository) {

    fun configurationFor(request: HttpServletRequest): AuthServerCorsConfiguration {
        val allowedOrigin = originFrom(request) ?: return AuthServerCorsConfiguration("")

        return if (allowedOrigin in allowedOriginRepository.getAllAvailableAllowedOrigins()) {
            AuthServerCorsConfiguration(allowedOrigin.content)
        } else {
            AuthServerCorsConfiguration("")
        }
    }


    private fun originFrom(request: HttpServletRequest): AllowedOrigin? {
        val header = request.getHeader("Origin")
        return header?.let { AllowedOrigin(it) }
    }

}

data class AuthServerCorsConfiguration(
    val allowedOrigin: String,
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS"),
    val maxAge: Long = 3600,
    val allowCredentials: Boolean = true
)
