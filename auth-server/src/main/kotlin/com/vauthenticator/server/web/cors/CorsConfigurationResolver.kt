package com.vauthenticator.server.web.cors

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import jakarta.servlet.http.HttpServletRequest

class CorsConfigurationResolver(private val allowedOriginRepository: AllowedOriginRepository) {

    fun configurationFor(request: HttpServletRequest): AuthServerCorsConfiguration {
        val allowedOrigin = originFrom(request)

        if (allowedOriginRepository.getAllAvailableAllowedOrigins().contains(allowedOrigin)) {
            return AuthServerCorsConfiguration(allowedOrigin = allowedOrigin!!.content)
        }

        return AuthServerCorsConfiguration(allowedOrigin = "")
    }


    private fun originFrom(request: HttpServletRequest): AllowedOrigin? {
        val header = request.getHeader("Origin")
        return header?.let { AllowedOrigin(it) }
    }

}

data class AuthServerCorsConfiguration(
    val allowedOrigin: String,
    val allowedMethods: List<String> = listOf("GET", "POST", "OPTIONS"),
    val maxAge: Long = 3600,
    val allowCredentials: Boolean = true
)