package com.vauthenticator.server.password.api

import com.vauthenticator.server.password.domain.PasswordGenerator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PasswordGeneratorEndPoint(private val passwordGenerator: PasswordGenerator) {

    @PostMapping("/api/password")
    fun generate() = ResponseEntity.ok(GeneratedPasswordResponse(passwordGenerator.generate()))
}

data class GeneratedPasswordResponse(val pwd: String)
