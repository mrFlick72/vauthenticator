package com.vauthenticator.server.document.domain

interface DocumentRepository {
    fun loadDocument(type: String, path: String): Document

    fun saveDocument(type: String, document: Document)

    fun documentKeyFor(type: String, path: String): String {
        return "${type}/${path}"
    }
}
