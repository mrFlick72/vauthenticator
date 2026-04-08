package com.vauthenticator.server.web

import com.vauthenticator.server.document.domain.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty("embedded-asset-cdn.enabled", havingValue = "true", matchIfMissing = true)
class StaticController(
    @Value("\${asset-server.on-s3.bundle-version:}") private val bundleVersionPath: String,
    private val staticAssetDocumentLocalCache: CaffeineCache,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StaticController::class.java)
    }

    @GetMapping("/static/content/asset/{assetName}")
    fun assetContent(@PathVariable assetName: String): ResponseEntity<ByteArray> {
        val finalAssetName = resolvedAssetName(assetName)
        logger.debug("assetName: {}", assetName)
        logger.debug("finalAssetName: {}", finalAssetName)

        return staticAssetDocumentLocalCache.get(finalAssetName, Document::class.java)
            ?.let { document ->
                ResponseEntity.ok()
                    .header("Content-Type", document.contentType)
                    .body(document.content)
            }
            ?: ResponseEntity.notFound().build()
    }

    private fun resolvedAssetName(assetName: String): String =
        if (bundleVersionPath.isBlank()) assetName else "$bundleVersionPath/$assetName"
}
