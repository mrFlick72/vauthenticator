package com.vauthenticator.server.document.adapter

import com.vauthenticator.server.document.adapter.DocumentUtils.initDocumentTests
import com.vauthenticator.server.document.adapter.DocumentUtils.s3Client
import com.vauthenticator.server.document.adapter.FileUtils.loadFileFor
import com.vauthenticator.server.document.domain.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.S3Client

internal class S3DocumentRepositoryTest {
    @Test
    fun load_document_from_S3() {
        val s3Client: S3Client = s3Client()
        initDocumentTests(s3Client)

        val documentRepository = S3DocumentRepository(
            s3Client,
            documentBucket
        )
        val document =
            documentRepository.loadDocument("mail", "templates/index.html")

        val expected: String = loadFileFor("/index.html")
        Assertions.assertEquals(expected, String(document.content))
    }

    @Test
    fun store_document_on_S3() {
        val s3Client: S3Client = s3Client()
        initDocumentTests(s3Client)

        val documentFile: String = loadFileFor("/index.html")
        val document = Document(
            "text/html",
            "templates/index.html",
            documentFile.toByteArray()
        )

        val documentRepository =
            S3DocumentRepository(
                s3Client,
                documentBucket
            )

        documentRepository.saveDocument("mail", document)

        val actual =
            documentRepository.loadDocument("mail", "templates/index.html")
        Assertions.assertEquals(actual, document)
    }
}