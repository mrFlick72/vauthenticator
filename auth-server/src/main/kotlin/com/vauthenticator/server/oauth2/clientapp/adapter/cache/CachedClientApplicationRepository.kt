package com.vauthenticator.server.oauth2.clientapp.adapter.cache

import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import java.util.*

class CachedClientApplicationRepository(
    private val cacheContentConverter: CacheContentConverter<ClientApplication>,
    private val cacheOperation: CacheOperation<String, String>,
    private val delegate: ClientApplicationRepository
) : ClientApplicationRepository by delegate {

    override fun findOne(clientAppId: ClientAppId): Optional<ClientApplication> {
        return cacheOperation.get(clientAppId.content)
            .flatMap { Optional.of(cacheContentConverter.getObjectFromCacheContentFor(it)) }
            .or {
                val clientApp = delegate.findOne(clientAppId)
                clientApp.ifPresent {
                    val loadableContentIntoCache = cacheContentConverter.loadableContentIntoCacheFor(it)
                    cacheOperation.put(clientAppId.content, loadableContentIntoCache)
                }
                clientApp
            }
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