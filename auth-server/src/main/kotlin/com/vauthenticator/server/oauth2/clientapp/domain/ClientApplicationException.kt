package com.vauthenticator.server.oauth2.clientapp.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vauthenticator.server.web.ValidationResults

class InsufficientClientApplicationScopeException(message: String) : RuntimeException(message)

class UnsupportedClientAppOperationException(message: String) : RuntimeException(message)

class InvalidAppDataException(message: String, val validationResults: ValidationResults) : RuntimeException(message) {

    companion object {
        fun exceptionFrom(content: ValidationResults): InvalidAppDataException =
            InvalidAppDataException(jacksonObjectMapper().writeValueAsString(content), content)
    }
}