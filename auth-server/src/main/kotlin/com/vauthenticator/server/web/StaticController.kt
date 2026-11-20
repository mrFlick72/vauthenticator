package com.vauthenticator.server.web

import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.page.domain.PageLocation
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty("embedded-asset-cdn.enabled", havingValue = "true", matchIfMissing = true)
class StaticController(
    private val pageLocation: PageLocation,
    private val staticAssetDocumentLocalCache: CaffeineCache,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(StaticController::class.java)
    }

    @GetMapping("/static/content/asset/{assetName}")
    fun assetContent(@PathVariable assetName: String): ResponseEntity<ByteArray> {
        val assetPath = pageLocation.pathOf(assetName)
        logger.debug("assetPath: {}", assetPath)

        return staticAssetDocumentLocalCache.get(assetPath, Document::class.java)
            ?.let { document ->
                ResponseEntity.ok()
                    .header("Content-Type", document.contentType)
                    .body(document.content)
            }
            ?: ResponseEntity.notFound().build()
    }
}
