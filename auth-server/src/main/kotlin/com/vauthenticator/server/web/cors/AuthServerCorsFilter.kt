package com.vauthenticator.server.web.cors

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class AuthServerCorsFilter(
    private val corsConfigurationResolver: CorsConfigurationResolver,
    private val allowedEndpoints: List<String>
) : OncePerRequestFilter() {


    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (allowedEndpoints.contains(request.requestURI)) {
            val corsConfiguration = corsConfigurationResolver.configurationFor(request)
            response.addHeader("Access-Control-Allow-Origin", corsConfiguration.allowedOrigin)
            response.addHeader("Access-Control-Allow-Methods", corsConfiguration.allowedMethods.joinToString(" "))
            response.addHeader("Access-Control-Max-Age", corsConfiguration.maxAge.toString())
            response.addHeader("Access-Control-Allow-Credentials", corsConfiguration.allowCredentials.toString())
        }
        filterChain.doFilter(request, response)
    }

}