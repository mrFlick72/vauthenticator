package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.web.ValidationResult
import com.vauthenticator.server.web.ValidationResults
import org.slf4j.LoggerFactory
import kotlin.collections.plusAssign

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
    private val logger = LoggerFactory.getLogger(ClientApplication::class.java)

    fun validate() {
        val errorMessaged = mutableMapOf<String, ValidationResult>()
        isClientAppIdValid(errorMessaged)
        isConfidential(errorMessaged)

        hasAtLeastOneGrantFlow(errorMessaged)
        hasAtLeastOneScope(errorMessaged)

        isAuthorizationCodeFLowValid(errorMessaged)
        areTokenTtlValid(errorMessaged)

        if (errorMessaged.isNotEmpty()) {
            logger.warn(errorMessaged.toString())
            throw InvalidAppDataException.exceptionFrom(errorMessaged)
        }
    }

    private fun hasAtLeastOneGrantFlow(errorMessaged: ValidationResults): ValidationResults {
        if (authorizedGrantTypes.content.isEmpty()) {
            errorMessaged += mapOf(
                "client_application.authorized_grant_types" to
                        ValidationResult(
                            errorsCode = listOf("client_application.authorized_grant_types.not_empty"),
                            errorMessage = "Client app ${clientAppId.content} has to have at least one authorized grant type"
                        )
            )

        }
        return errorMessaged
    }

    private fun hasAtLeastOneScope(errorMessaged: ValidationResults): ValidationResults {
        if (scopes.content.isEmpty()) {
            errorMessaged += mapOf(
                "client_application.scopes" to
                        ValidationResult(
                            errorsCode = listOf("client_application.scopes.not_empty"),
                            errorMessage = "Client app ${clientAppId.content} has to have at least one scope"
                        )
            )
        }
        return errorMessaged
    }

    private fun areTokenTtlValid(errorMessaged: ValidationResults): ValidationResults {
        if (accessTokenValidity.content <= 0) {
            errorMessaged += mapOf(
                "client_application.access_token.ttl" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.access_token.ttl.not_empty",
                                "client_application.access_token.ttl.gtn_zero"
                            ),
                            errorMessage = "Client app ${clientAppId.content} has access token ttl <= 0"
                        )
            )
        }
        if (authorizedGrantTypes.content.contains(AuthorizedGrantType.REFRESH_TOKEN) && refreshTokenValidity.content <= 0) {
            errorMessaged += mapOf(
                "client_application.refresh_token.ttl" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.refresh_token.not_empty",
                                "client_application.refresh_token.ttl.gtn_zero"
                            ),
                            errorMessage = "Client app ${clientAppId.content} support Refresh Token FLow but refresh token ttl is <= 0"
                        )
            )
        }
        return errorMessaged
    }

    private fun isAuthorizationCodeFLowValid(errorMessaged: ValidationResults): ValidationResults {
        if (authorizedGrantTypes.content.contains(AuthorizedGrantType.AUTHORIZATION_CODE)) {
            if (webServerRedirectUri.content.isBlank() or webServerRedirectUri.content.isEmpty()) {
                errorMessaged += mapOf(
                    "client_application.callback_uri" to
                            ValidationResult(
                                errorsCode = listOf(
                                    "client_application.callback_uri.not_empty",
                                    "client_application.callback_uri.not_blank"
                                ),
                                errorMessage = "Client app ${clientAppId.content} support Authorization Code FLow but the redirect uri is blank empty"
                            )
                )
            }
            if (postLogoutRedirectUri.content.isBlank() or postLogoutRedirectUri.content.isEmpty()) {
                errorMessaged += mapOf(
                    "client_application.post_logout_redirect_uri" to
                            ValidationResult(
                                errorsCode = listOf(
                                    "client_application.post_logout_redirect_uri.not_empty",
                                    "client_application.post_logout_redirect_uri.not_blank"
                                ),
                                errorMessage = "Client app ${clientAppId.content} support Authorization Code FLow but the post logout redirect uri is blank empty"
                            )
                )
            }

            if (logoutUri.content.isBlank() or logoutUri.content.isEmpty()) {
                errorMessaged += mapOf(
                    "client_application.logout_uri" to
                            ValidationResult(
                                errorsCode = listOf(
                                    "client_application.logout_uri.not_empty",
                                    "client_application.logout_uri.not_blank"
                                ),
                                errorMessage = "Client app ${clientAppId.content} support Authorization Code FLow but the logout uri is blank empty"
                            )
                )
            }
        }
        return errorMessaged
    }

    private fun isClientAppIdValid(errorMessaged: ValidationResults): ValidationResults {
        if (clientAppId.content.isBlank() or clientAppId.content.isEmpty()) {
            errorMessaged += mapOf(
                "client_application.id" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.id.not_empty",
                                "client_application.id.not_blank"
                            ),
                            errorMessage = "Client app id cannot be blank or empty"
                        )

            )
        }
        if (clientAppId.content.lowercase() == "null") {
            errorMessaged += mapOf(
                "client_application.id" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.id.invalid",
                            ),
                            errorMessage = "Client app id cannot be the string 'null'"
                        )

            )
        }

        if (clientAppId.content.lowercase() == "undefined") {
            errorMessaged += mapOf(
                "client_application.id" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.id.invalid",
                            ),
                            errorMessage = "Client app id cannot be the string 'undefined'"
                        )

            )
        }

        return errorMessaged
    }

    private fun isConfidential(errorMessaged: ValidationResults): ValidationResults {
        if (confidential && secret.content.isBlank()) {
            errorMessaged += mapOf(
                "client_application.secret" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.logout_uri.not_empty",
                                "client_application.logout_uri.not_blank"
                            ),
                            errorMessage = "Client app ${clientAppId.content} secret is empty or blank and it is not supported for confidential client applications"
                        )
            )
        }

        if (!confidential && secret.content.isNotBlank()) {
            errorMessaged += mapOf(
                "client_application.secret" to
                        ValidationResult(
                            errorsCode = listOf(
                                "client_application.secret.empty",
                                "client_application.secret.blank"
                            ),
                            errorMessage = "Client app ${clientAppId.content} secret is not empty or blank and it is not supported for public client applications"
                        )

            )
        }
        return errorMessaged
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
        fun from(vararg authorizedGrantType: AuthorizedGrantType) =
            AuthorizedGrantTypes(listOf(*authorizedGrantType))
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