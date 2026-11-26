package com.vauthenticator.platform.document.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Where a server keeps its Documents. Other keys under `document`, such as a server's own cache settings, are
 * left to that server.
 */
@ConfigurationProperties("document")
data class DocumentProperties(
    val engine: DocumentEngine? = null,
    val bucketName: String? = null,
    val fsBasePath: String? = null
)

/** Bound from the `document.engine` values `s3` and `file-system`. */
enum class DocumentEngine(val propertyValue: String) {
    S3("s3"), FILE_SYSTEM("file-system")
}
