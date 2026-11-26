package com.vauthenticator.server.management.init

import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.platform.document.domain.DocumentRepository
import com.vauthenticator.server.page.domain.PageDocumentType
import com.vauthenticator.server.page.domain.Page
import com.vauthenticator.server.page.domain.PageLocation
import org.springframework.core.io.ClassPathResource

class DefaultPageNotFoundException(message: String) : RuntimeException(message)

/**
 * Saves the Default Page of every Page, built into the jar under `pages/default`, at the path the pages are served
 * from. It replaces any page already there: tenant setup is meant for a new tenant only.
 */
class DefaultPageSetUpJob(
    private val documentRepository: DocumentRepository,
    private val pageLocation: PageLocation,
    private val defaultPagesClassPath: String = "pages/default"
) {

    fun execute() {
        Page.entries.forEach { page ->
            val defaultPage = ClassPathResource("$defaultPagesClassPath/${page.bundle}")
            if (!defaultPage.exists()) {
                throw DefaultPageNotFoundException(
                    "The Default Page ${page.bundle} is not in the jar: was it built without the frontend (-Dskip.npm)?"
                )
            }
            documentRepository.saveDocument(
                PageDocumentType.STATIC_ASSET.content,
                Document(JAVASCRIPT, pageLocation.pathOf(page.bundle), defaultPage.contentAsByteArray)
            )
        }
    }

    private companion object {
        const val JAVASCRIPT = "application/javascript"
    }
}
