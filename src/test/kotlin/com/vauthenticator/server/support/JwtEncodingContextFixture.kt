package com.vauthenticator.server.support

import com.vauthenticator.server.support.SecurityFixture.principalFor
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext

object JwtEncodingContextFixture {

    private val registeredClient = RegisteredClient.withId("client_id")
        .clientId("client_id")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("http://localhost/calback")
        .build()

    private val authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
        .principalName(EMAIL)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .attributes { it["java.security.Principal"] = principalFor(EMAIL) }
        .build()

    val newContext: JwtEncodingContext = JwtEncodingContext.with(
        JwsHeader.with(MacAlgorithm.HS256),
        JwtClaimsSet.builder()
    )
        .tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .authorization(authorization)
        .registeredClient(registeredClient)
        .build()


    val newClientCredentialsContext: JwtEncodingContext = JwtEncodingContext.with(
        JwsHeader.with(MacAlgorithm.HS256),
        JwtClaimsSet.builder()
    )
        .tokenType(OAuth2TokenType.ACCESS_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .authorization(authorization)
        .registeredClient(registeredClient)
        .build()
}