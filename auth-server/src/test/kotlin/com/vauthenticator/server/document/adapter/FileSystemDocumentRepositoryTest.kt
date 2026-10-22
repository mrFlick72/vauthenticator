package com.vauthenticator.server.document.adapter

import com.vauthenticator.server.document.adapter.FileUtils.loadFileFor
import com.vauthenticator.server.document.domain.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.NoSuchFileException
import java.nio.file.Path

internal class FileSystemDocumentRepositoryTest {
    private val documentRepository: FileSystemDocumentRepository = FileSystemDocumentRepository("src/test/resources")

    @Test
    fun load_document_from_FileSystem() {
        val document: Document = documentRepository.loadDocument("mail", "/index.html")

        val expected: String = loadFileFor("/mail/index.html")
        Assertions.assertEquals(expected, String(document.content))
    }

    @Test
    fun `saving a document replaces the existing one`(@TempDir basePath: Path) {
        val uut = FileSystemDocumentRepository(basePath.toString())
        uut.saveDocument("sms", Document("text/plain", "templates/mfa-challenge.txt", "old text".toByteArray()))

        uut.saveDocument("sms", Document("text/plain", "templates/mfa-challenge.txt", "new text".toByteArray()))

        val actual = uut.loadDocument("sms", "templates/mfa-challenge.txt")
        Assertions.assertEquals("new text", String(actual.content))
    }

    @Test
    fun `loading a missing document fails`(@TempDir basePath: Path) {
        val uut = FileSystemDocumentRepository(basePath.toString())

        Assertions.assertThrows(NoSuchFileException::class.java) {
            uut.loadDocument("sms", "templates/mfa-challenge.txt")
        }
    }
}
