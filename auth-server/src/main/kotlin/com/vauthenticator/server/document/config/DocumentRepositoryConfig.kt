package com.vauthenticator.server.document.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.vauthenticator.server.document.adapter.FileSystemDocumentRepository
import com.vauthenticator.server.document.adapter.S3DocumentRepository
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3Client
import java.time.Duration

@Configuration(proxyBeanMethods = false)
internal class DocumentRepositoryConfig {
    @Bean("documentRepository")
    @ConditionalOnProperty(value = ["document.engine"], havingValue = "s3")
    fun s3DocumentRepository(
        @Value("\${document.bucket-name}") documentBucketName: String,
        s3Client: S3Client
    ): DocumentRepository {
        return S3DocumentRepository(s3Client, documentBucketName)
    }


    @Bean("documentRepository")
    @ConditionalOnProperty(value = ["document.engine"], havingValue = "file-system")
    fun fileSystemDocumentRepository(@Value("\${document.fs-base-path}") basePath: String): DocumentRepository {
        return FileSystemDocumentRepository(basePath)
    }


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
                        DocumentType.EMAIL.content,
                        "templates/${assetName}"
                    )
                })
    }

    @Bean
    fun staticAssetDocumentLocalCache(
        documentRepository: DocumentRepository,
        @Value("\${document.document-type.static-asset.cache.name:static-asset-document-local-cache}") cacheName: String,
        @Value("\${document.document-type.static-asset.cache.ttl:1m}") ttl: Duration
    ): CaffeineCache {
        return CaffeineCache(
            cacheName, Caffeine.newBuilder()
                .refreshAfterWrite(ttl)
                .build { assetName: Any? ->
                    documentRepository.loadDocument(
                        DocumentType.STATIC_ASSET.content,
                        "content/asset/${assetName}"
                    )
                })
    }
}