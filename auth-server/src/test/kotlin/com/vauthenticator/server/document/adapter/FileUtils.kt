package com.vauthenticator.server.document.adapter

import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths


object FileUtils {
    fun loadFileFor(path: String): String {
        try {
            val resource: URL = DocumentUtils::class.java.getResource(path)
            val resourcePath = Paths.get(resource.toURI())
            val bytes = Files.readAllBytes(resourcePath)

            return String(bytes)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
