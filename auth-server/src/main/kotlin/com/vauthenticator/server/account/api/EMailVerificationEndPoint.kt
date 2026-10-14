package com.vauthenticator.server.account.api

import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallenge
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
class EMailVerificationEndPoint(
    private val permissionValidator: PermissionValidator,
    private val sendVerifyEMailChallenge: SendVerifyEMailChallenge
) {

    @PutMapping("/api/verify-challenge")
    fun sendVerifyMail(
        @RequestBody request: EMailVerificationRequest,
        authentication: Authentication?
    ): ResponseEntity<Unit> {
        permissionValidator.validate(authentication, Scopes.from(Scope.MAIL_VERIFY))
        sendVerifyEMailChallenge.sendVerifyMail(request.email)
        return noContent().build()
    }

}

data class EMailVerificationRequest(val email: String)