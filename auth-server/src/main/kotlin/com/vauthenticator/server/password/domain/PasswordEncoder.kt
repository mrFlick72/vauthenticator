package com.vauthenticator.server.password.domain

interface VAuthenticatorPasswordEncoder {
    fun encode(password: String): String

    fun matches(password: String, encodedPassword: String): Boolean
}

class CredentialEncodingException(message: String) : RuntimeException(message)
