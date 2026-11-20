package com.vauthenticator.server.management.init

import com.vauthenticator.server.document.adapter.FileSystemDocumentRepository
import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.page.domain.Page
import com.vauthenticator.server.page.domain.PageLocation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/**
 * Reads the Default Pages Maven builds into target/classes/pages/default: run the tests after a build with the
 * frontend (not -Dskip.npm).
 */
class DefaultPageSetUpJobTest {

    @Test
    fun `tenant setup gives every page its default page where pages are served from`(@TempDir basePath: Path) {
        val documentRepository = FileSystemDocumentRepository(basePath.toString())
        val pageLocation = PageLocation("")
        val uut = DefaultPageSetUpJob(documentRepository, pageLocation)

        uut.execute()

        Page.entries.forEach {
            val page = documentRepository.loadDocument(DocumentType.STATIC_ASSET.content, pageLocation.pathOf(it.bundle))
            assertThat(page.content).isEqualTo(defaultPage(it))
        }
    }

    @Test
    fun `tenant setup saves the default pages under the bundle version`(@TempDir basePath: Path) {
        val documentRepository = FileSystemDocumentRepository(basePath.toString())
        val uut = DefaultPageSetUpJob(documentRepository, PageLocation("0.10"))

        uut.execute()

        val login = documentRepository.loadDocument(DocumentType.STATIC_ASSET.content, "content/asset/0.10/login_bundle.js")
        assertThat(login.content).isEqualTo(defaultPage(Page.LOGIN))
    }

    @Test
    fun `tenant setup replaces an existing page with its default page`(@TempDir basePath: Path) {
        val documentRepository = FileSystemDocumentRepository(basePath.toString())
        val pageLocation = PageLocation("")
        val uut = DefaultPageSetUpJob(documentRepository, pageLocation)
        documentRepository.saveDocument(
            DocumentType.STATIC_ASSET.content,
            Document("application/javascript", pageLocation.pathOf(Page.LOGIN.bundle), "an operator's login".toByteArray())
        )

        uut.execute()

        val login = documentRepository.loadDocument(DocumentType.STATIC_ASSET.content, pageLocation.pathOf(Page.LOGIN.bundle))
        assertThat(login.content).isEqualTo(defaultPage(Page.LOGIN))
    }

    @Test
    fun `tenant setup fails when a default page is not in the jar`(@TempDir basePath: Path) {
        val uut = DefaultPageSetUpJob(FileSystemDocumentRepository(basePath.toString()), PageLocation(""), "no-such-pages")

        assertThrows<DefaultPageNotFoundException> { uut.execute() }
    }

    private fun defaultPage(page: Page): ByteArray =
        Path.of("target/classes/pages/default/${page.bundle}").toFile().readBytes()
}
