package com.vauthenticator.server.management.init

import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.communication.domain.SmsType
import com.vauthenticator.platform.document.adapter.FileSystemDocumentRepository
import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.server.communication.domain.CommunicationDocumentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class CommunicationTemplateSetUpJobTest {

    @Test
    fun `tenant setup gives every email and sms type its default template`(@TempDir basePath: Path) {
        val documentRepository = FileSystemDocumentRepository(basePath.toString())
        val uut = CommunicationTemplateSetUpJob(documentRepository)

        uut.execute()

        EMailType.entries.forEach {
            val template = documentRepository.loadDocument(CommunicationDocumentType.EMAIL.content, it.path)
            assertThat(String(template.content)).isEqualTo(defaultTemplate("email", it.path))
        }
        SmsType.entries.forEach {
            val template = documentRepository.loadDocument(CommunicationDocumentType.SMS.content, it.path)
            assertThat(String(template.content)).isEqualTo(defaultTemplate("sms", it.path))
        }
    }

    @Test
    fun `tenant setup replaces an existing template with its default template`(@TempDir basePath: Path) {
        val documentRepository = FileSystemDocumentRepository(basePath.toString())
        val uut = CommunicationTemplateSetUpJob(documentRepository)
        documentRepository.saveDocument(
            CommunicationDocumentType.EMAIL.content,
            Document("text/html", EMailType.WELCOME.path, "an admin's welcome".toByteArray())
        )
        documentRepository.saveDocument(
            CommunicationDocumentType.SMS.content,
            Document("text/plain", SmsType.MFA.path, "an admin's code {{ mfaCode }}".toByteArray())
        )

        uut.execute()

        val welcome = documentRepository.loadDocument(CommunicationDocumentType.EMAIL.content, EMailType.WELCOME.path)
        assertThat(String(welcome.content)).isEqualTo(defaultTemplate("email", EMailType.WELCOME.path))
        val mfaSms = documentRepository.loadDocument(CommunicationDocumentType.SMS.content, SmsType.MFA.path)
        assertThat(String(mfaSms.content)).isEqualTo(defaultTemplate("sms", SmsType.MFA.path))
    }

    private fun defaultTemplate(channel: String, path: String): String {
        val fileName = path.substringAfterLast("/")
        return Path.of("src/main/resources/communication/default/$channel/$fileName").toFile().readText()
    }
}
