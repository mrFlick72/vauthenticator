package com.vauthenticator.server.oauth2.clientapp.adapter.cache

import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import java.util.Optional

class CachedClientApplicationRepository(
    private val cacheContentConverter: CacheContentConverter<ClientApplication>,
    private val cacheOperation: CacheOperation<String, String>,
    private val delegate: ClientApplicationRepository
) : ClientApplicationRepository by delegate {

    override fun findOne(clientAppId: ClientAppId): Optional<ClientApplication> {
        val cachedClientApplication = cacheOperation.get(clientAppId.content)
            ?.let { cacheContentConverter.getObjectFromCacheContentFor(it) }
        if (cachedClientApplication != null) {
            return Optional.of(cachedClientApplication)
        }

        val clientApp = delegate.findOne(clientAppId)
        clientApp.ifPresent {
            val loadableContentIntoCache = cacheContentConverter.loadableContentIntoCacheFor(it)
            cacheOperation.put(clientAppId.content, loadableContentIntoCache)
        }
        return clientApp
    }

    override fun save(clientApp: ClientApplication) {
        cacheOperation.evict(clientApp.clientAppId.content)
        delegate.save(clientApp)
    }

    override fun delete(clientAppId: ClientAppId) {
        cacheOperation.evict(clientAppId.content)
        delegate.delete(clientAppId)
    }
}
