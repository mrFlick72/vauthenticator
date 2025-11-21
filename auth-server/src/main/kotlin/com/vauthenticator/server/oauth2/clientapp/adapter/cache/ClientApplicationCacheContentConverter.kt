package com.vauthenticator.server.oauth2.clientapp.adapter.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.oauth2.clientapp.domain.*

class ClientApplicationCacheContentConverter(private val objectMapper: ObjectMapper) :
    CacheContentConverter<ClientApplication> {
    override fun getObjectFromCacheContentFor(cacheContent: String): ClientApplication =
        objectMapper.readValue(cacheContent, Map::class.java)
            .let {
                ClientApplication(
                    clientAppId = ClientAppId(it["clientAppId"] as String),
                    clientAppName = ClientAppName(it["clientAppName"] as String),
                    secret = Secret(it["secret"] as String),
                    scopes = Scopes((it["scopes"] as List<String>).map { scope -> Scope(scope) }.toSet()),
                    authorizedGrantTypes = AuthorizedGrantTypes(
                        (it["authorizedGrantTypes"] as List<String>).map(
                            AuthorizedGrantType::valueOf
                        )
                    ),
                    webServerRedirectUri = CallbackUri(it["webServerRedirectUri"] as String),
                    allowedOrigins = AllowedOrigins((it["allowedOrigins"] as List<String>).map { AllowedOrigin(it) }.toSet()),
                    accessTokenValidity = TokenTimeToLive(it["accessTokenValidity"].toString().toLong()),
                    refreshTokenValidity = TokenTimeToLive(it["refreshTokenValidity"].toString().toLong()),
                    additionalInformation = it["additionalInformation"] as Map<String, Any>,
                    autoApprove = AutoApprove(it["autoApprove"] as Boolean),
                    postLogoutRedirectUri = PostLogoutRedirectUri(it["postLogoutRedirectUri"] as String),
                    logoutUri = LogoutUri(it["logoutUri"] as String)
                )

            }


    override fun loadableContentIntoCacheFor(source: ClientApplication): String =
        objectMapper.writeValueAsString(
            mapOf(
                "clientAppId" to source.clientAppId.content,
                "clientAppName" to source.clientAppName.content,
                "secret" to source.secret.content,
                "scopes" to source.scopes.content.map(Scope::content),
                "authorizedGrantTypes" to source.authorizedGrantTypes.content.map(AuthorizedGrantType::name),
                "webServerRedirectUri" to source.webServerRedirectUri.content,
                "allowedOrigins" to source.allowedOrigins.content.map(AllowedOrigin::content),
                "accessTokenValidity" to source.accessTokenValidity.content,
                "refreshTokenValidity" to source.refreshTokenValidity.content,
                "additionalInformation" to source.additionalInformation,
                "autoApprove" to source.autoApprove.content,
                "postLogoutRedirectUri" to source.postLogoutRedirectUri.content,
                "logoutUri" to source.logoutUri.content
            )
        )

}