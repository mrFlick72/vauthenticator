package com.vauthenticator.server.management.init

import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.AuthorizedGrantType.*
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.AVAILABLE_SCOPES
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.MFA_ALWAYS
import com.vauthenticator.server.oauth2.clientapp.domain.WithPkce.Companion.disabled
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder

class ClientApplicationSetUpJob(
    private val clientApplicationRepository: ClientApplicationRepository,
    private val passwordEncoder: VAuthenticatorPasswordEncoder
) {
    fun execute() {
        clientApplicationRepository.save(m2mDefaultAdminClientApp())
        clientApplicationRepository.save(managementUIDefaultClientApp())
    }

    private fun managementUIDefaultClientApp() = ClientApplication(
        clientAppId = ClientAppId("vauthenticator-management-ui"),
        clientAppName = ClientAppName("vauthenticator-management-ui"),
        secret = Secret(passwordEncoder.encode("secret")),
        scopes = Scopes.from(*(AVAILABLE_SCOPES - MFA_ALWAYS).toTypedArray()),
        withPkce = disabled,
        authorizedGrantTypes = AuthorizedGrantTypes.from(AUTHORIZATION_CODE, REFRESH_TOKEN),
        webServerRedirectUri = CallbackUri("http://local.management.vauthenticator.com:8080/login/oauth2/code/client"),
        allowedOrigins = AllowedOrigins.empty(),
        accessTokenValidity = TokenTimeToLive(3600),
        refreshTokenValidity = TokenTimeToLive(3600),
        additionalInformation = emptyMap(),
        autoApprove = AutoApprove.approve,
        postLogoutRedirectUri = PostLogoutRedirectUri("http://local.management.vauthenticator.com:8080/secure/admin/index"),
        logoutUri = LogoutUri("http://local.management.vauthenticator.com:8080/logout"),
    )

    private fun m2mDefaultAdminClientApp() = ClientApplication(
        clientAppId = ClientAppId("admin"),
        clientAppName = ClientAppName("admin"),
        secret = Secret(passwordEncoder.encode("secret")),
        scopes = Scopes.from(*(AVAILABLE_SCOPES - MFA_ALWAYS).toTypedArray()),
        withPkce = disabled,
        authorizedGrantTypes = AuthorizedGrantTypes.from(CLIENT_CREDENTIALS),
        allowedOrigins = AllowedOrigins.empty(),
        webServerRedirectUri = CallbackUri(""),
        accessTokenValidity = TokenTimeToLive(3600),
        refreshTokenValidity = TokenTimeToLive(3600),
        additionalInformation = emptyMap(),
        autoApprove = AutoApprove.approve,
        postLogoutRedirectUri = PostLogoutRedirectUri(""),
        logoutUri = LogoutUri("")
    )
}