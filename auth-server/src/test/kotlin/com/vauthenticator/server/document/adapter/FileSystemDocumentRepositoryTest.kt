package com.vauthenticator.server.document.adapter

import com.vauthenticator.server.document.adapter.FileUtils.loadFileFor
import com.vauthenticator.server.document.domain.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FileSystemDocumentRepositoryTest {
    private val documentRepository: FileSystemDocumentRepository = FileSystemDocumentRepository("src/test/resources")

    @Test
    fun load_document_from_FileSystem() {
        val document: Document = documentRepository.loadDocument("mail", "/index.html")

        val expected: String = loadFileFor("/mail/index.html")
        Assertions.assertEquals(expected, String(document.content))
    }

    @Test
    fun store_document_on_FileSystem() {
        val documentFile: String = loadFileFor("/mail/index.html")
        val document = Document("text/html", "/index.html", documentFile.toByteArray())

        documentRepository.saveDocument("mail", document)

        val actual = documentRepository.loadDocument("mail", "/index.html")
        Assertions.assertEquals(String(actual.content), String(document.content))
    }
}