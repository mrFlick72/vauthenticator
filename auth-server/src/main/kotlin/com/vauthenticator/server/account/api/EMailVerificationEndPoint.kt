package com.vauthenticator.server.account.api

import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallenge
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
class MailVerificationEndPoint(
    private val permissionValidator: PermissionValidator,
    private val sendVerifyEMailChallenge: SendVerifyEMailChallenge
) {

    @PutMapping("/api/verify-challenge")
    fun sendVerifyMail(
        @RequestBody request: Map<String, String>,
        httpSession: HttpSession,
        principal: JwtAuthenticationToken
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, httpSession, Scopes.from(Scope.MAIL_VERIFY))

        return if (request.keys.contains("email")) {
            val email = request["email"]!!
            sendVerifyEMailChallenge.sendVerifyMail(email)
            noContent().build()
        } else {
            badRequest().build()
        }

    }

}