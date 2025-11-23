package com.vauthenticator.server.communication.api

import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.communication.domain.EMailTemplate
import com.vauthenticator.server.communication.domain.EMailType
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class EMailEndPoint(private val documentRepository: DocumentRepository) {

    @GetMapping("/api/email-template/{emailType}")
    fun getMailTemplate(@PathVariable emailType: EMailType): ResponseEntity<EMailTemplate> {
        val document = documentRepository.loadDocument(DocumentType.EMAIL.content, emailType.path)
        return ResponseEntity.ok(EMailTemplate(emailType, String(document.content)))
    }

    @PutMapping("/api/email-template")
    fun saveMailTemplate(@RequestBody request: EMailTemplate): ResponseEntity<Unit> {
        documentRepository.saveDocument(
            DocumentType.EMAIL.content, //todo MAIL should be EMAIL
            Document(
                MediaType.TEXT_HTML_VALUE,
                request.emailType.path,
                request.body.toByteArray()
            )
        )
        return ResponseEntity.noContent().build()
    }
}