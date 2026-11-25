package com.vauthenticator.server.communication

import com.github.benmanes.caffeine.cache.Caffeine
import com.vauthenticator.platform.document.domain.DocumentRepository
import com.vauthenticator.server.communication.domain.CommunicationDocumentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration(proxyBeanMethods = false)
class CommunicationDocumentCacheConfig {

    @Bean
    fun mailDocumentLocalCache(
        documentRepository: DocumentRepository,
        @Value("\${document.document-type.mail.cache.name:mail-document-local-cache}") cacheName: String,
        @Value("\${document.document-type.mail.cache.ttl:1m}") ttl: Duration
    ): CaffeineCache {
        return CaffeineCache(
            cacheName, Caffeine.newBuilder()
                .refreshAfterWrite(ttl)
                .build { assetName: Any? ->
                    documentRepository.loadDocument(
                        CommunicationDocumentType.EMAIL.content,
                        "templates/${assetName}"
                    )
                })
    }
}
