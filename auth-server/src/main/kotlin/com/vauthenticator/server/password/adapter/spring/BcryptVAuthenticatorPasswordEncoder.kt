package com.vauthenticator.server.password.adapter.spring

import com.vauthenticator.server.password.domain.CredentialEncodingException
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class BcryptVAuthenticatorPasswordEncoder(private val passwordEncoder: PasswordEncoder) :
    VAuthenticatorPasswordEncoder {

    override fun encode(password: String): String =
        passwordEncoder.encode(password)
            ?: throw CredentialEncodingException("Unable to encode credential using BcryptVAuthenticatorPasswordEncoder")

    override fun matches(password: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(password, encodedPassword)
}
