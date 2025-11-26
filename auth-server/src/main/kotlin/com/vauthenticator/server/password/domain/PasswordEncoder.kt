package com.vauthenticator.server.password.domain

//TODO consider to revisit it due to spring enforce that the operation can return a nullable result
interface VAuthenticatorPasswordEncoder {
    fun encode(password: String): String

    fun matches(password: String, encodedPassword: String): Boolean
}

