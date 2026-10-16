package com.vauthenticator.server.password.api

import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.password.domain.PasswordGenerator
import com.vauthenticator.server.role.domain.PermissionValidator
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PasswordGeneratorEndPoint(
    private val permissionValidator: PermissionValidator,
    private val passwordGenerator: PasswordGenerator
) {

    @PostMapping("/api/password")
    fun generate(authentication: Authentication?): ResponseEntity<GeneratedPasswordResponse> {
        permissionValidator.validate(authentication, Scopes.from(Scope.GENERATE_PASSWORD))
        return ResponseEntity.ok(GeneratedPasswordResponse(passwordGenerator.generate()))
    }
}

data class GeneratedPasswordResponse(val pwd: String)
