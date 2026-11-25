package com.vauthenticator.server.management.init

import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.communication.domain.SmsType
import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.platform.document.domain.DocumentRepository
import com.vauthenticator.server.communication.domain.CommunicationDocumentType
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType

/**
 * Saves the Default Template of every Email Type and SMS Type, shipped in the jar under `communication/default`.
 * It replaces any template already there: tenant setup is meant for a new tenant only.
 */
class CommunicationTemplateSetUpJob(private val documentRepository: DocumentRepository) {

    fun execute() {
        EMailType.entries.forEach { seed(CommunicationDocumentType.EMAIL, MediaType.TEXT_HTML_VALUE, it.path) }
        SmsType.entries.forEach { seed(CommunicationDocumentType.SMS, MediaType.TEXT_PLAIN_VALUE, it.path) }
    }

    private fun seed(documentType: CommunicationDocumentType, contentType: String, path: String) {
        val defaultTemplate = ClassPathResource("communication/default/${documentType.content}/${path.substringAfterLast("/")}")
        documentRepository.saveDocument(
            documentType.content,
            Document(contentType, path, defaultTemplate.contentAsByteArray)
        )
    }
}
