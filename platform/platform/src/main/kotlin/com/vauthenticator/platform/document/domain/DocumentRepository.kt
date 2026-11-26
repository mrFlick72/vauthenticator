package com.vauthenticator.platform.document.domain

/**
 * Stores the Documents of one server. The type is the server's own Document Type name: the platform only uses it
 * to keep kinds apart, as the first segment of the storage key.
 */
interface DocumentRepository {
    fun loadDocument(type: String, path: String): Document

    fun saveDocument(type: String, document: Document)

    fun documentKeyFor(type: String, path: String): String {
        return "${type}/${path}"
    }
}
