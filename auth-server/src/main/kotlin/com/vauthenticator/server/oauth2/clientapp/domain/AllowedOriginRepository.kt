package com.vauthenticator.server.oauth2.clientapp.domain

interface AllowedOriginRepository {

    fun getAllAvailableAllowedOrigins(): Set<AllowedOrigin>

    fun setAllowedOriginsFor(clientAppId:ClientAppId, allowedOrigins: AllowedOrigins)

    fun deleteAllowedOriginsFor(clientAppId: ClientAppId)
}