package com.vauthenticator.server.oauth2.clientapp.domain

data class ClientApplication(
    val clientAppId: ClientAppId,
    val clientAppName: ClientAppName,
    val secret: Secret,
    val confidential: Boolean = true,
    val scopes: Scopes,
    val withPkce: WithPkce = WithPkce.disabled,
    val authorizedGrantTypes: AuthorizedGrantTypes,
    val webServerRedirectUri: CallbackUri,
    val allowedOrigins: AllowedOrigins,
    val accessTokenValidity: TokenTimeToLive,
    val refreshTokenValidity: TokenTimeToLive,
    val additionalInformation: Map<String, Any> = emptyMap(),
    val autoApprove: AutoApprove = AutoApprove.approve,
    val postLogoutRedirectUri: PostLogoutRedirectUri,
    val logoutUri: LogoutUri,
) {
    fun validate() {
        isClientAppIdValid()
        isConfidential()

        isAuthorizationCodeFLowValid()
        areTokenTtlValid()
    }

    private fun areTokenTtlValid() {
        if (authorizedGrantTypes.content.contains(AuthorizedGrantType.AUTHORIZATION_CODE)) {
            if (accessTokenValidity.content > 0) {
                throw InvalidAppDataException("Client app ${clientAppId.content} has access token ttl < 0")
            }
            if (authorizedGrantTypes.content.contains(AuthorizedGrantType.REFRESH_TOKEN) && refreshTokenValidity.content > 0) {
                throw InvalidAppDataException("Client app ${clientAppId.content} support Refresh Token FLow but refresh token ttl is < 0")
            }
        }
    }

    private fun isAuthorizationCodeFLowValid() {
        if (authorizedGrantTypes.content.contains(AuthorizedGrantType.AUTHORIZATION_CODE)) {
            if (webServerRedirectUri.content.isBlank() or webServerRedirectUri.content.isEmpty()) {
                throw InvalidAppDataException("Client app ${clientAppId.content} support Authorization Code FLow but the redirect uri is blank empty")
            }
            if (postLogoutRedirectUri.content.isBlank() or postLogoutRedirectUri.content.isEmpty()) {
                throw InvalidAppDataException("Client app ${clientAppId.content} support Authorization Code FLow but the post logout redirect uri is blank empty")
            }
            if (logoutUri.content.isBlank() or logoutUri.content.isEmpty()) {
                throw InvalidAppDataException("Client app ${clientAppId.content} support Authorization Code FLow but the logout uri is blank empty")
            }
        }
    }

    private fun isClientAppIdValid() {
        if (clientAppId.content.isBlank() or clientAppId.content.isEmpty()) {
            throw InvalidAppDataException("Client app id cannot be blank or empty")
        }
    }

    private fun isConfidential() {
        if (confidential && secret.content.isBlank()) {
            throw UnsupportedClientAppOperationException("Client app %${clientAppId} secret is empty or blank and it is not supported for confidential client applications")
        }

        if (!confidential && secret.content.isNotBlank()) {
            throw UnsupportedClientAppOperationException("Client app %${clientAppId} secret is not empty or blank and it is not supported for public client applications")
        }
    }
}

@JvmInline
value class ClientAppName(val content: String)

@JvmInline
value class WithPkce(val content: Boolean) {
    companion object {
        val enabled = WithPkce(true)
        val disabled = WithPkce(false)
    }
}

data class AutoApprove(val content: Boolean) {
    companion object {
        val approve = AutoApprove(true)
        val disapprove = AutoApprove(false)
    }
}

data class AuthorizedGrantTypes(val content: List<AuthorizedGrantType>) {
    companion object {
        fun from(vararg authorizedGrantType: AuthorizedGrantType) = AuthorizedGrantTypes(listOf(*authorizedGrantType))
    }
}

enum class AuthorizedGrantType { CLIENT_CREDENTIALS, AUTHORIZATION_CODE, REFRESH_TOKEN }

data class Secret(val content: String)

data class ClientAppId(val content: String) {
    companion object {
        fun empty(): ClientAppId = ClientAppId("")
    }
}

data class AllowedOrigins(val content: Set<AllowedOrigin>) {
    companion object {
        fun empty() = AllowedOrigins(setOf(AllowedOrigin("*")))
        fun from(vararg allowedOrigin: AllowedOrigin) = AllowedOrigins(setOf(*allowedOrigin))
    }
}

data class AllowedOrigin(val content: String)
data class CallbackUri(val content: String)
data class PostLogoutRedirectUri(val content: String)
data class LogoutUri(val content: String)

data class Scopes(val content: Set<Scope>) {
    companion object {
        fun from(vararg scope: Scope) = Scopes(setOf(*scope))
    }
}

data class Scope(val content: String) {
    companion object {
        val OPEN_ID = Scope("openid")
        val PROFILE = Scope("profile")
        val EMAIL = Scope("email")

        val ADMIN_FULL_ACCESS = Scope("admin:full-access")

        val SIGN_UP = Scope("admin:signup")
        val WELCOME = Scope("admin:welcome")

        val MAIL_VERIFY = Scope("admin:email-verify")

        val GENERATE_PASSWORD = Scope("admin:generate-password")
        val RESET_PASSWORD = Scope("admin:reset-password")
        val CHANGE_PASSWORD = Scope("admin:change-password")

        val MAIL_TEMPLATE_READER = Scope("admin:email-template-reader")
        val MAIL_TEMPLATE_WRITER = Scope("admin:email-template-writer")

        val KEY_READER = Scope("admin:key-reader")
        val KEY_EDITOR = Scope("admin:key-editor")

        val MFA_ALWAYS = Scope("mfa:always")
        val MFA_ENROLLMENT = Scope("mfa:enrollment")

        val READ_CLIENT_APPLICATION = Scope("admin:client-app-reader")
        val SAVE_CLIENT_APPLICATION = Scope("admin:client-app-writer")
        val DELETE_CLIENT_APPLICATION = Scope("admin:client-app-eraser")

        val AVAILABLE_SCOPES = listOf(
            OPEN_ID,
            PROFILE,
            EMAIL,

            ADMIN_FULL_ACCESS,

            SIGN_UP,
            WELCOME,
            MAIL_VERIFY,
            RESET_PASSWORD,
            CHANGE_PASSWORD,
            GENERATE_PASSWORD,

            KEY_READER,
            KEY_EDITOR,

            MAIL_TEMPLATE_READER,
            MAIL_TEMPLATE_WRITER,

            MFA_ALWAYS,
            MFA_ENROLLMENT,

            READ_CLIENT_APPLICATION,
            SAVE_CLIENT_APPLICATION,
            DELETE_CLIENT_APPLICATION
        )

    }
}

data class Authorities(val content: Set<Authority>) {
    companion object {
        fun empty() = Authorities(emptySet())
    }
}

data class Authority(val content: String)
data class TokenTimeToLive(val content: Long)

enum class ClientApplicationFeatures(val value: String) {
    SIGNUP("signup"),
    RESET_PASSWORD("reset-password")
}