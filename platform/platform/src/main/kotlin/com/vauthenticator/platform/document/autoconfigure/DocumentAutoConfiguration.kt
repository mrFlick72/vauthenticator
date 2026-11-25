package com.vauthenticator.platform.document.autoconfigure

import com.vauthenticator.platform.document.adapter.FileSystemDocumentRepository
import com.vauthenticator.platform.document.adapter.S3DocumentRepository
import com.vauthenticator.platform.document.domain.DocumentRepository
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.util.ClassUtils
import software.amazon.awssdk.services.s3.S3Client

private const val ENGINE_KEY = "document.engine"
private const val S3_CLIENT_CLASS = "software.amazon.awssdk.services.s3.S3Client"

/**
 * Provides the server's [DocumentRepository] from `document.engine`. A server or a test that defines its own
 * [DocumentRepository] turns it off. Any missing or wrong setting stops the startup, naming the key to fix.
 */
@AutoConfiguration
@ConditionalOnMissingBean(DocumentRepository::class)
@EnableConfigurationProperties(DocumentProperties::class)
class DocumentAutoConfiguration {

    @Bean
    fun documentRepository(properties: DocumentProperties, context: ApplicationContext): DocumentRepository {
        val engine = properties.engine
            ?: throw DocumentStorageConfigurationException(
                "$ENGINE_KEY is not set: set it to one of ${DocumentEngine.entries.joinToString { it.propertyValue }}"
            )
        return when (engine) {
            DocumentEngine.FILE_SYSTEM ->
                FileSystemDocumentRepository(required(properties.fsBasePath, "document.fs-base-path", engine))

            DocumentEngine.S3 -> {
                val bucketName = required(properties.bucketName, "document.bucket-name", engine)
                if (!ClassUtils.isPresent(S3_CLIENT_CLASS, context.classLoader)) {
                    throw DocumentStorageConfigurationException(
                        "$ENGINE_KEY is s3 but the AWS SDK S3 client is not on the classpath: add software.amazon.awssdk:s3"
                    )
                }
                S3DocumentRepositories.create(context, bucketName)
            }
        }
    }

    private fun required(value: String?, key: String, engine: DocumentEngine): String =
        value?.takeIf { it.isNotBlank() }
            ?: throw DocumentStorageConfigurationException(
                "$key is not set: it is required when $ENGINE_KEY is ${engine.propertyValue}"
            )
}

/** Kept apart so that the AWS SDK classes are only loaded when the engine is s3. */
private object S3DocumentRepositories {
    fun create(beanFactory: BeanFactory, bucketName: String): DocumentRepository {
        val s3Client = beanFactory.getBeanProvider(S3Client::class.java).ifAvailable
            ?: throw DocumentStorageConfigurationException(
                "$ENGINE_KEY is s3 but there is no S3Client bean: the server must define one"
            )
        return S3DocumentRepository(s3Client, bucketName)
    }
}
