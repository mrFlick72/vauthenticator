package com.vauthenticator.server.document.adapter

import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentRepository
import java.nio.file.Files
import java.nio.file.Paths


class FileSystemDocumentRepository(private val basePath: String) : DocumentRepository {
    override fun loadDocument(type: String, path: String): Document {
        val filePath = Paths.get(basePath, documentKeyFor(type, path))
        val contentType = contentTypeFor(filePath)
        val content = readAllBytesFrom(filePath)
        return Document(contentType, path, content)
    }

    override fun saveDocument(type: String, document: Document) {
        val filePath = Paths.get(basePath, documentKeyFor(type, document.path))
        try {
            Files.createFile(filePath)
            Files.write(filePath, document.content)
        } catch (e: Exception) {
        }
    }
}
