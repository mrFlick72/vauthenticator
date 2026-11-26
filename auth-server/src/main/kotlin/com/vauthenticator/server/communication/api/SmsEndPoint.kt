package com.vauthenticator.server.communication.api

import com.vauthenticator.server.communication.domain.SmsTemplate
import com.vauthenticator.server.communication.domain.SmsType
import com.vauthenticator.platform.document.domain.Document
import com.vauthenticator.platform.document.domain.DocumentRepository
import com.vauthenticator.server.communication.domain.CommunicationDocumentType
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
class SmsEndPoint(
    private val documentRepository: DocumentRepository,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/sms-template/{smsType}")
    fun getSmsTemplate(
        authentication: Authentication?,
        @PathVariable smsType: SmsType
    ): ResponseEntity<SmsTemplate> {
        permissionValidator.validate(authentication, Scopes.from(Scope.SMS_TEMPLATE_READER))

        val document = documentRepository.loadDocument(CommunicationDocumentType.SMS.content, smsType.path)
        return ResponseEntity.ok(SmsTemplate(smsType, String(document.content)))
    }

    @PutMapping("/api/sms-template")
    fun saveSmsTemplate(authentication: Authentication?, @RequestBody request: SmsTemplate): ResponseEntity<Unit> {
        permissionValidator.validate(authentication, Scopes.from(Scope.SMS_TEMPLATE_WRITER))

        documentRepository.saveDocument(
            CommunicationDocumentType.SMS.content,
            Document(
                MediaType.TEXT_PLAIN_VALUE,
                request.smsType.path,
                request.body.toByteArray()
            )
        )
        return ResponseEntity.noContent().build()
    }
}
