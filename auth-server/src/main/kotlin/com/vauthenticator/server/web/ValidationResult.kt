package com.vauthenticator.server.web

typealias ValidationResults = MutableMap<String, ValidationResult>

data class ValidationResult(
    val errorMessage: String,
    val errorsCode: List<String>
) {}