package com.vauthenticator.server.page

import com.github.benmanes.caffeine.cache.Caffeine
import com.vauthenticator.platform.document.domain.DocumentRepository
import com.vauthenticator.server.page.domain.PageDocumentType
import com.vauthenticator.server.page.domain.PageLocation
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class PageConfig {

    @Bean
    fun pageLocation(@Value("\${asset-server.on-s3.bundle-version:}") bundleVersion: String) =
        PageLocation(bundleVersion)

    @Bean
    fun staticAssetDocumentLocalCache(
        documentRepository: DocumentRepository,
        @Value("\${document.document-type.static-asset.cache.name:static-asset-document-local-cache}") cacheName: String,
        @Value("\${document.document-type.static-asset.cache.ttl:1m}") ttl: Duration
    ): CaffeineCache {
        return CaffeineCache(
            cacheName, Caffeine.newBuilder()
                .refreshAfterWrite(ttl)
                // the key is the asset's full path in the storage, resolved by PageLocation
                .build { assetPath: Any? ->
                    documentRepository.loadDocument(PageDocumentType.STATIC_ASSET.content, assetPath as String)
                })
    }
}
