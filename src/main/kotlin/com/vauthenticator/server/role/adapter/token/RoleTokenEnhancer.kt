package com.vauthenticator.server.role.adapter.token

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import java.util.stream.Collectors

class RoleTokenEnhancer(
    val tokenType: String,
    val roleClaimName: String,
) : OAuth2TokenCustomizer<JwtEncodingContext> {

    override fun customize(context: JwtEncodingContext) {

        println("RoleTokenEnhancer before the if")
        if (isATokenForAUserFrom(context)) {
            println("RoleTokenEnhancer after the if")
            if (tokenType == context.tokenType.value) {
                val attributes = context.authorization!!.attributes
                val principal = attributes["java.security.Principal"] as Authentication

                context.claims.claim(
                    roleClaimName, principal.authorities
                        .stream()
                        .map { obj: GrantedAuthority -> obj.authority }
                        .collect(Collectors.toList()))
            }
        }
    }

    private fun isATokenForAUserFrom(context: JwtEncodingContext): Boolean =
        !context.authorizationGrantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS)
}