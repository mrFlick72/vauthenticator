package com.vauthenticator.server.oidc.token

import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.Kid
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

class IdTokenEnhancer(
    private val assignedKeys: MutableSet<Kid>,
    private val keyRepository: KeyRepository
) : OAuth2TokenCustomizer<JwtEncodingContext> {
    override fun customize(context: JwtEncodingContext) {
        val tokenType = context.tokenType.value

        if ("id_token" == tokenType && !context.authorizationGrantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
            val signatureKey = keyRepository.signatureKeys().peekOneAtRandomWithout(assignedKeys)
            context.jwsHeader.keyId(signatureKey.kid.content())

            val attributes = context.authorization!!.attributes
            val principle = attributes["java.security.Principal"] as Authentication
            context.claims.claim("email", principle.name)
        }
    }
}