package com.vauthenticator.platform.document.autoconfigure

import com.vauthenticator.platform.document.adapter.FileSystemDocumentRepository
import com.vauthenticator.platform.document.adapter.S3DocumentRepository
import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.platform.document.domain.DocumentRepository
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.FilteredClassLoader
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import software.amazon.awssdk.services.s3.S3Client

class DocumentAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DocumentAutoConfiguration::class.java))

    @Test
    fun `the file-system engine stores documents under the base path`() {
        contextRunner
            .withPropertyValues(
                "document.engine=file-system",
                "document.fs-base-path=dist",
                // a server's own settings under the same prefix are left alone
                "document.document-type.mail.cache.ttl=1m"
            )
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(DocumentRepository::class.java))
                    .isInstanceOf(FileSystemDocumentRepository::class.java)
            }
    }

    @Test
    fun `the s3 engine stores documents in the bucket through the server's S3Client`() {
        contextRunner
            .withPropertyValues("document.engine=s3", "document.bucket-name=documents")
            .withBean(S3Client::class.java, { mockk<S3Client>() })
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(DocumentRepository::class.java))
                    .isInstanceOf(S3DocumentRepository::class.java)
            }
    }

    @Test
    fun `a missing engine stops the startup`() {
        contextRunner
            .run { context -> assertFailsNaming(context, "document.engine is not set: set it to one of s3, file-system") }
    }

    @Test
    fun `an unknown engine stops the startup`() {
        contextRunner
            .withPropertyValues("document.engine=ftp")
            .run { context ->
                assertThat(context).hasFailed()
                assertThat(context.startupFailure).hasStackTraceContaining("document.engine")
            }
    }

    @Test
    fun `the file-system engine without a base path stops the startup`() {
        contextRunner
            .withPropertyValues("document.engine=file-system")
            .run { context -> assertFailsNaming(context, "document.fs-base-path is not set") }
    }

    @Test
    fun `the s3 engine without a bucket stops the startup`() {
        contextRunner
            .withPropertyValues("document.engine=s3", "document.bucket-name=")
            .withBean(S3Client::class.java, { mockk<S3Client>() })
            .run { context -> assertFailsNaming(context, "document.bucket-name is not set") }
    }

    @Test
    fun `the s3 engine without an S3Client bean stops the startup`() {
        contextRunner
            .withPropertyValues("document.engine=s3", "document.bucket-name=documents")
            .run { context -> assertFailsNaming(context, "there is no S3Client bean") }
    }

    @Test
    fun `the s3 engine without the AWS SDK stops the startup`() {
        contextRunner
            .withClassLoader(FilteredClassLoader(S3Client::class.java))
            .withPropertyValues("document.engine=s3", "document.bucket-name=documents")
            .run { context -> assertFailsNaming(context, "the AWS SDK S3 client is not on the classpath") }
    }

    @Test
    fun `a server's own document repository replaces the starter's, with no document settings`() {
        val ownRepository = object : DocumentRepository {
            override fun loadDocument(type: String, path: String) = Document("text/plain", path, ByteArray(0))
            override fun saveDocument(type: String, document: Document) {}
        }

        contextRunner
            .withBean(DocumentRepository::class.java, { ownRepository })
            .run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(DocumentRepository::class.java)).isSameAs(ownRepository)
            }
    }

    private fun assertFailsNaming(context: AssertableApplicationContext, message: String) {
        assertThat(context).hasFailed()
        assertThat(context.startupFailure)
            .rootCause()
            .isInstanceOf(DocumentStorageConfigurationException::class.java)
            .hasMessageContaining(message)
    }
}
