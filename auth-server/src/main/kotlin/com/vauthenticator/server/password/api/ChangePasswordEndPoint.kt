package com.vauthenticator.server.password.api

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.PasswordPolicyViolation
import com.vauthenticator.server.password.domain.changepassword.ChangePassword
import com.vauthenticator.server.password.domain.changepassword.ChangePasswordRequest
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.internalServerError
import org.springframework.http.ResponseEntity.noContent
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChangePasswordEndPoint(
    private val permissionValidator: PermissionValidator,
    val changePassword: ChangePassword
) {

    @PutMapping("/api/accounts/password")
    fun sendVerifyMail(
        @RequestBody request: ChangePasswordRequestRepresentation,
        authentication: Authentication?
    ): ResponseEntity<Unit> {
        val principal = permissionValidator.validate(authentication, Scopes.from(Scope.CHANGE_PASSWORD))
        changePassword.resetPasswordFor(principal, ChangePasswordRequest(request.pwd))
        return noContent().build()
    }

    @ExceptionHandler(AccountNotFoundException::class, PasswordPolicyViolation::class)
    fun exceptionHandler(): ResponseEntity<Unit> {
        return internalServerError().build()
    }
}

data class ChangePasswordRequestRepresentation(val pwd: String)
