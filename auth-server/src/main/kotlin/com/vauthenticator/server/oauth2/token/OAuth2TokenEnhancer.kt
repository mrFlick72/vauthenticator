package com.vauthenticator.server.oauth2.token

import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

class OAuth2TokenEnhancer(
    private val assignedKeys: MutableSet<Kid>,
    private val keyRepository: KeyRepository,
    private val clientApplicationRepository: ClientApplicationRepository
) : OAuth2TokenCustomizer<JwtEncodingContext> {
    override fun customize(context: JwtEncodingContext) {
        val tokenType = context.tokenType.value
        if ("access_token" == tokenType) {
            val signatureKey = keyRepository.signatureKeys().peekOneAtRandomWithout(assignedKeys)
            context.jwsHeader.keyId(signatureKey.kid.content())

            if (context.authorizationGrantType.equals(AuthorizationGrantType.CLIENT_CREDENTIALS)) {
                val clientId = context.registeredClient.clientId
                val findOne = clientApplicationRepository.findOne(ClientAppId(clientId))
                findOne.ifPresent {
                    context.claims.claim("user_name", it.clientAppId.content)
                }
            } else {
                val attributes = context.authorization!!.attributes
                val principal = attributes["java.security.Principal"] as Authentication
                context.claims.claim("user_name", principal.name)
            }

        }
    }
}