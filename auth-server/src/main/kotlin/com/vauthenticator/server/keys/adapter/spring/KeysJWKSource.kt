package com.vauthenticator.server.keys.adapter.spring

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import com.vauthenticator.server.keys.domain.KeyDecrypter
import com.vauthenticator.server.keys.domain.KeyRepository

class KeysJWKSource(
    private val keyDecrypter: KeyDecrypter,
    private val keyRepository: KeyRepository
) : JWKSource<SecurityContext?> {
    override fun get(jwkSelector: JWKSelector, context: SecurityContext?): MutableList<JWK> {
        val rsaKey: List<RSAKey> = keyRepository.signatureKeys().generateRsas(keyDecrypter)
        val jwkSet = JWKSet(rsaKey)
        return jwkSelector.select(jwkSet)
    }
}