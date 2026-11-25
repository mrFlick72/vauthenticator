package com.vauthenticator.platform.document.adapter

import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.platform.document.domain.DocumentRepository
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class FileSystemDocumentRepository(private val basePath: String) : DocumentRepository {
    override fun loadDocument(type: String, path: String): Document {
        val filePath = Paths.get(basePath, documentKeyFor(type, path))
        val content = Files.readAllBytes(filePath)
        val contentType = contentTypeFor(filePath)
        return Document(contentType, path, content)
    }

    override fun saveDocument(type: String, document: Document) {
        val filePath = Paths.get(basePath, documentKeyFor(type, document.path))
        Files.createDirectories(filePath.parent)
        Files.write(filePath, document.content)
    }
}

private fun contentTypeFor(filePath: Path): String {
    val contentType: String
    try {
        contentType = Files.probeContentType(filePath)
    } catch (e: IOException) {
        throw RuntimeException(e)
    }
    return contentType
}
