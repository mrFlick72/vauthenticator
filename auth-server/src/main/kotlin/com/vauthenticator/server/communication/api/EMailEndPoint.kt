package com.vauthenticator.server.communication.api

import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.communication.domain.EMailTemplate
import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class EMailEndPoint(
    private val documentRepository: DocumentRepository,
    private val permissionValidator: PermissionValidator
) {

    @GetMapping("/api/email-template/{emailType}")
    fun getMailTemplate(
        principal: JwtAuthenticationToken,
        @PathVariable emailType: EMailType
    ): ResponseEntity<EMailTemplate> {
        permissionValidator.validate(principal, Scopes.from(Scope.MAIL_TEMPLATE_READER))

        val document = documentRepository.loadDocument(DocumentType.EMAIL.content, emailType.path)
        return ResponseEntity.ok(EMailTemplate(emailType, String(document.content)))
    }

    @PutMapping("/api/email-template")
    fun saveMailTemplate(principal: JwtAuthenticationToken, @RequestBody request: EMailTemplate): ResponseEntity<Unit> {
        permissionValidator.validate(principal, Scopes.from(Scope.MAIL_TEMPLATE_WRITER))

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