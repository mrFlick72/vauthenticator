package com.vauthenticator.server.role.adapter.token

import com.vauthenticator.server.extentions.isATokenForAUserFrom
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

class RoleTokenEnhancer(
    val tokenType: String,
    val roleClaimName: String,
) : OAuth2TokenCustomizer<JwtEncodingContext> {


    override fun customize(context: JwtEncodingContext) {

        if (context.isATokenForAUserFrom()) {
            if (tokenType == context.tokenType.value) {
                val attributes = context.authorization?.attributes
                attributes?.let {
                    val principal = it["java.security.Principal"] as Authentication

                    context.claims.claim(
                        roleClaimName,
                        principal.authorities.map { authority -> authority.authority },
                    )
                }
            }
        }
    }

}