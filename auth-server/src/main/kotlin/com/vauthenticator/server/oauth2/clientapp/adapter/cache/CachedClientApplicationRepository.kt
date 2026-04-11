package com.vauthenticator.server.oauth2.clientapp.adapter.cache

import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository

class CachedClientApplicationRepository(
    private val cacheContentConverter: CacheContentConverter<ClientApplication>,
    private val cacheOperation: CacheOperation<String, String>,
    private val delegate: ClientApplicationRepository
) : ClientApplicationRepository by delegate {

    override fun findOne(clientAppId: ClientAppId): ClientApplication? {
        val cachedClientApplication = cacheOperation.get(clientAppId.content)
            ?.let { cacheContentConverter.getObjectFromCacheContentFor(it) }
        if (cachedClientApplication != null) {
            return cachedClientApplication
        }

        val clientApp = delegate.findOne(clientAppId)
        if (clientApp != null) {
            val loadableContentIntoCache = cacheContentConverter.loadableContentIntoCacheFor(clientApp)
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
