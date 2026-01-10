package com.vauthenticator.server.oauth2.clientapp.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vauthenticator.server.web.ValidationResult

class InsufficientClientApplicationScopeException(message: String) : RuntimeException(message)

class UnsupportedClientAppOperationException(message: String) : RuntimeException(message)

class InvalidAppDataException(message: String) : RuntimeException(message) {
    val validationResult: ValidationResult = mutableMapOf()

    companion object {
        fun exceptionFrom(content: ValidationResult): InvalidAppDataException =
            InvalidAppDataException(jacksonObjectMapper().writeValueAsString(content))
    }
}