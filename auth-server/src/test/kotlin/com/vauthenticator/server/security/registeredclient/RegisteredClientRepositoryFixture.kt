package com.vauthenticator.server.security.registeredclient

import com.vauthenticator.server.oauth2.clientapp.domain.*
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import java.time.Duration
import java.util.*

private const val A_CLIENT_APP_ID = "A_CLIENT_APP_ID"
private const val A_CLIENT_APP_NAME_CLIENT_APP_NAME = "A_CLIENT_APP_NAME"

object RegisteredClientRepositoryFixture {

    fun aRegisteredClient(confidential: Boolean): RegisteredClient = RegisteredClient.withId(A_CLIENT_APP_ID)
        .clientId(A_CLIENT_APP_ID)
        .clientName(A_CLIENT_APP_NAME_CLIENT_APP_NAME)
        .clientSecret("A_CLIENT_APP_SECRET")
        .clientSecret("A_SECRET")
        .clientAuthenticationMethods {
            if(confidential){
                it.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                it.add(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            }else{
                it.add(ClientAuthenticationMethod.NONE)
            }
        }
        .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .scope("A_SCOPE")
        .scope("ANOTHER_SCOPE")
        .redirectUri("http://a_call_back")
        .postLogoutRedirectUri("http://post_logout_redirect_uri")
        .clientSettings(
            ClientSettings.builder()
            .build())
        .tokenSettings(
            TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(100))
                .refreshTokenTimeToLive(Duration.ofSeconds(200))
                .reuseRefreshTokens(true)
                .build()
        )
        .build()

    fun aClientApplication(confidential : Boolean = true): Optional<ClientApplication> = Optional.ofNullable(
        ClientApplication(
            clientAppId = ClientAppId(A_CLIENT_APP_ID),
            clientAppName = ClientAppName(A_CLIENT_APP_NAME_CLIENT_APP_NAME),
            secret = Secret("A_SECRET"),
            scopes = Scopes(setOf(Scope("A_SCOPE"), Scope("ANOTHER_SCOPE"))),
            authorizedGrantTypes = AuthorizedGrantTypes(
                listOf(
                    AuthorizedGrantType.REFRESH_TOKEN,
                    AuthorizedGrantType.AUTHORIZATION_CODE
                )
            ),
            confidential = confidential,
            webServerRedirectUri = CallbackUri("http://a_call_back"),
            allowedOrigins = AllowedOrigins(setOf(AllowedOrigin("*"))),
            accessTokenValidity = TokenTimeToLive(100),
            refreshTokenValidity = TokenTimeToLive(200),
            additionalInformation = emptyMap(),
            autoApprove = AutoApprove.approve,
            postLogoutRedirectUri = PostLogoutRedirectUri("http://post_logout_redirect_uri"),
            logoutUri = LogoutUri("http://logout_uri")
        )
    )
}