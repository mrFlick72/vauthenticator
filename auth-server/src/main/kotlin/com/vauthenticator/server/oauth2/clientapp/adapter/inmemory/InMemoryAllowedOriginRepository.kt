package com.vauthenticator.server.oauth2.clientapp.adapter.inmemory

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigins
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId

class InMemoryAllowedOriginRepository(val storage: MutableMap<ClientAppId, AllowedOrigins>) :
    AllowedOriginRepository {

    override fun getAllAvailableAllowedOrigins(): Set<AllowedOrigin> {
        return storage.flatMap { it.value.content.toList() }.toSet()
    }

    override fun setAllowedOriginsFor(clientAppId: ClientAppId, allowedOrigins: AllowedOrigins) {
        storage.put(clientAppId, allowedOrigins)
    }

    override fun deleteAllowedOriginsFor(clientAppId: ClientAppId) {
        storage.remove(clientAppId)
    }

}