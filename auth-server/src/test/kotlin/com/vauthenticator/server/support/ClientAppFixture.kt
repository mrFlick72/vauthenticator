package com.vauthenticator.server.support

import com.vauthenticator.server.oauth2.clientapp.domain.*

const val A_CLIENT_APP_ID = "A_CLIENT_APP_ID"

object ClientAppFixture {
    fun aClientApp(
        clientAppId: ClientAppId,
        confidential: Boolean = true,
        password: Secret = Secret("secret"),
        logoutUri: LogoutUri = LogoutUri("http://an_uri"),
        allowedOrigins: AllowedOrigins = AllowedOrigins.from(AllowedOrigin("*"))
    ) = ClientApplication(
        clientAppId,
        ClientAppName("$clientAppId-client-app-name"),
        password,
        confidential,
        Scopes.from(Scope.EMAIL, Scope.OPEN_ID, Scope.PROFILE, Scope.RESET_PASSWORD),
        WithPkce.enabled,
        AuthorizedGrantTypes.from(AuthorizedGrantType.CLIENT_CREDENTIALS),
        CallbackUri("http://an_uri"),
        allowedOrigins,
        TokenTimeToLive(10),
        TokenTimeToLive(10),
        emptyMap(),
        AutoApprove.approve,
        PostLogoutRedirectUri("http://an_uri"),
        logoutUri
    )

    fun aClientAppId(): ClientAppId = ClientAppId(A_CLIENT_APP_ID)

}