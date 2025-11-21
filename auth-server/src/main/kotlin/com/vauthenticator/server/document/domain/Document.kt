package com.vauthenticator.server.document.domain

import java.util.*

@JvmRecord
data class Document(val contentType: String, val path: String, val content: ByteArray) {
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val document = o as Document
        return contentType == document.contentType && path == document.path && content.contentEquals(document.content)
    }

    override fun hashCode(): Int {
        var result = Objects.hash(contentType, path)
        result = 31 * result + content.contentHashCode()
        return result
    }
}

enum class DocumentType(val content: String) {
    EMAIL("email"), STATIC_ASSET("static-asset")
}
