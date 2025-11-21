package com.vauthenticator.server.oauth2.clientapp.adapter.jdbc

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.oauth2.clientapp.domain.*
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.sql.ResultSet
import java.util.*

private const val FINED_ALL_QUERY = """
    SELECT client_app_id,
    client_app_name,
    secret,
    confidential,
    scopes,
    with_pkce,
    authorized_grant_types,
    web_server_redirect_uri,
    allowed_origins,
    access_token_validity,
    refresh_token_validity,
    additional_information,
    auto_approve,
    post_logout_redirect_uri,
logout_uri FROM CLIENT_APPLICATION
"""
private const val FINED_ONE_QUERY = """
    $FINED_ALL_QUERY WHERE client_app_id=:client_app_id
"""
private const val SAVE_QUERY = """
    INSERT INTO CLIENT_APPLICATION (
    client_app_id,
    client_app_name,
    secret,
    confidential,
    scopes,
    with_pkce,
    authorized_grant_types,
    web_server_redirect_uri,
    allowed_origins,
    access_token_validity,
    refresh_token_validity,
    additional_information,
    auto_approve,
    post_logout_redirect_uri,
    logout_uri)
     VALUES (:client_app_id,:client_app_name,:secret,:confidential,:scopes,:with_pkce,:authorized_grant_types,:web_server_redirect_uri,:allowed_origins,:access_token_validity,:refresh_token_validity,:additional_information,:auto_approve,:post_logout_redirect_uri,:logout_uri)  ON CONFLICT(client_app_id) DO UPDATE SET secret=:secret,
    client_app_name=:client_app_name,
    confidential=:confidential,
    scopes=:scopes,
    with_pkce=:with_pkce,
    authorized_grant_types=:authorized_grant_types,
    web_server_redirect_uri=:web_server_redirect_uri,
    allowed_origins=:allowed_origins,
    access_token_validity=:access_token_validity,
    refresh_token_validity=:refresh_token_validity,
    additional_information=:additional_information,
    auto_approve=:auto_approve,
    post_logout_redirect_uri=:post_logout_redirect_uri,
logout_uri=:logout_uri
"""
private const val DELETE_QUERY = """DELETE FROM CLIENT_APPLICATION WHERE client_app_id=:client_app_id"""

class JdbcClientApplicationRepository(
    private val namedJdbcTemplate: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper,
    private val allowedOriginRepository: AllowedOriginRepository
) :
    ClientApplicationRepository {
    init {
        findAll().forEach { allowedOriginRepository.setAllowedOriginsFor(it.clientAppId, it.allowedOrigins) }
    }

    override fun findOne(clientAppId: ClientAppId): Optional<ClientApplication> {
        val queryResult =
            namedJdbcTemplate.query(FINED_ONE_QUERY, mapOf("client_app_id" to clientAppId.content)) { rs, _ ->
                JdbcClientApplicationConverter.fromDbToDomain(rs, objectMapper)
            }
        return Optional.ofNullable(queryResult.firstOrNull())
    }

    override fun findAll(): Iterable<ClientApplication> {
        return namedJdbcTemplate.query(FINED_ALL_QUERY) { rs, _ ->
            JdbcClientApplicationConverter.fromDbToDomain(rs, objectMapper)
        }
    }

    override fun save(clientApp: ClientApplication) {
        namedJdbcTemplate
            .update(
                SAVE_QUERY,
                mapOf(
                    "client_app_id" to clientApp.clientAppId.content,
                    "client_app_name" to clientApp.clientAppName.content,
                    "secret" to clientApp.secret.content,
                    "confidential" to clientApp.confidential,
                    "scopes" to
                            clientApp.scopes.content.joinToString(separator = ",") { it.content },
                    "with_pkce" to clientApp.withPkce.content,
                    "authorized_grant_types" to clientApp.authorizedGrantTypes.content.joinToString(separator = ",") { it.name },
                    "web_server_redirect_uri" to clientApp.webServerRedirectUri.content,
                    "allowed_origins" to clientApp.allowedOrigins.content.joinToString(separator = ",") { it.content },
                    "access_token_validity" to clientApp.accessTokenValidity.content,
                    "refresh_token_validity" to clientApp.refreshTokenValidity.content,
                    "additional_information" to objectMapper.writeValueAsString(clientApp.additionalInformation),
                    "auto_approve" to clientApp.autoApprove.content,
                    "post_logout_redirect_uri" to clientApp.postLogoutRedirectUri.content,
                    "logout_uri" to clientApp.logoutUri.content
                )
            )
        allowedOriginRepository.setAllowedOriginsFor(clientApp.clientAppId, clientApp.allowedOrigins)
    }

    override fun delete(clientAppId: ClientAppId) {
        namedJdbcTemplate.update(DELETE_QUERY, mapOf("client_app_id" to clientAppId.content))
        allowedOriginRepository.deleteAllowedOriginsFor(clientAppId)
    }

}

object JdbcClientApplicationConverter {

    fun fromDbToDomain(rs: ResultSet, objectMapper: ObjectMapper) = ClientApplication(
        clientAppId = ClientAppId(rs.getString("client_app_id")),
        clientAppName = ClientAppName(rs.getString("client_app_name")),
        secret = Secret(rs.getString("secret")),
        confidential = rs.getBoolean("confidential"),
        scopes = Scopes(rs.getString("scopes").split(",").map { Scope(it.trim()) }.toSet()),
        withPkce = WithPkce(rs.getBoolean("with_pkce")),
        authorizedGrantTypes = AuthorizedGrantTypes(
            rs.getString("authorized_grant_types").split(",").map { AuthorizedGrantType.valueOf(it) }),
        webServerRedirectUri = CallbackUri(rs.getString("web_server_redirect_uri")),
        allowedOrigins = AllowedOrigins(rs.getString("allowed_origins").split(",").map { AllowedOrigin(it) }.toSet()),
        accessTokenValidity = TokenTimeToLive(rs.getLong("access_token_validity")),
        refreshTokenValidity = TokenTimeToLive(rs.getLong("refresh_token_validity")),
        additionalInformation = Optional.ofNullable(
            objectMapper.readValue(
                rs.getString("additional_information"),
                Map::class.java
            ) as Map<String, String>
        ).orElse(emptyMap()),
        autoApprove = AutoApprove(rs.getBoolean("auto_approve")),
        postLogoutRedirectUri = PostLogoutRedirectUri(rs.getString("post_logout_redirect_uri")),
        logoutUri = LogoutUri(rs.getString("logout_uri"))
    )
}