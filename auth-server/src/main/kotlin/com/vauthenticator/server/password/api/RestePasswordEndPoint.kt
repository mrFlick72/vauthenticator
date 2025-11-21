package com.vauthenticator.server.password.api

import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.resetpassword.ResetAccountPassword
import com.vauthenticator.server.password.domain.resetpassword.SendResetPasswordMailChallenge
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.ticket.domain.TicketId
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@SessionAttributes("clientId")
class ResetPasswordEndPoint(
    private val permissionValidator: PermissionValidator,
    private val sendResetPasswordMailChallenge: SendResetPasswordMailChallenge,
    private val resetAccountPassword: ResetAccountPassword
) {

    @PutMapping("/api/reset-password-challenge")
    fun sendVerifyMail(
        @RequestBody request: Map<String, String>,
        session: HttpSession,
        principal: JwtAuthenticationToken?
    ): ResponseEntity<Unit> {
        permissionValidator.validate(principal, session, Scopes.from(Scope.RESET_PASSWORD))
        sendResetPasswordMailChallenge.sendResetPasswordMailFor(request["email"]!!)
        return noContent().build()
    }

    @PutMapping("/api/reset-password/{ticket}")
    fun resetPassword(@PathVariable ticket: String, @RequestBody request: ResetPasswordRequest): ResponseEntity<Unit> {
        resetAccountPassword.resetPasswordFromMailChallenge(TicketId(ticket), request)
        return noContent().build()
    }

}

data class ResetPasswordRequest(val newPassword: String)