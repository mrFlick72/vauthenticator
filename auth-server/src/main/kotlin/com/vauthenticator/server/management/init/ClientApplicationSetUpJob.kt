package com.vauthenticator.server.management.init

import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.AuthorizedGrantType.*
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.AVAILABLE_SCOPES
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.EMAIL
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.MFA_ALWAYS
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.OPEN_ID
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.PROFILE
import com.vauthenticator.server.oauth2.clientapp.domain.WithPkce.Companion.disabled
import com.vauthenticator.server.oauth2.clientapp.domain.WithPkce.Companion.enabled
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
        confidential = false,
        secret = Secret(""),
        scopes = Scopes.from(OPEN_ID, EMAIL, PROFILE),
        withPkce = enabled,
        authorizedGrantTypes = AuthorizedGrantTypes.from(AUTHORIZATION_CODE),
        webServerRedirectUri = CallbackUri("http://local.management.vauthenticator.com:8085/callback"),
        allowedOrigins = AllowedOrigins(setOf(AllowedOrigin("http://local.management.vauthenticator.com:8085"))),
        accessTokenValidity = TokenTimeToLive(3600),
        refreshTokenValidity = TokenTimeToLive(3600),
        additionalInformation = emptyMap(),
        autoApprove = AutoApprove.approve,
        postLogoutRedirectUri = PostLogoutRedirectUri("http://local.management.vauthenticator.com:8085/secure/admin/index"),
        logoutUri = LogoutUri("http://local.management.vauthenticator.com:8085/logout"),
    )

    private fun m2mDefaultAdminClientApp() = ClientApplication(
        clientAppId = ClientAppId("admin"),
        clientAppName = ClientAppName("admin"),
        secret = Secret(passwordEncoder.encode("secret")),
        scopes = Scopes.from(ADMIN_FULL_ACCESS),
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