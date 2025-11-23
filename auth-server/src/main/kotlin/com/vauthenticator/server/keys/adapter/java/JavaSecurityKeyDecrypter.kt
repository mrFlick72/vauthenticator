package com.vauthenticator.server.keys.adapter.java

import com.vauthenticator.server.extentions.encoder
import com.vauthenticator.server.keys.domain.KeyDecrypter
import com.vauthenticator.server.keys.domain.MasterKid

class JavaSecurityKeyDecrypter(
    private val maserKid: String,
    private val javaSecurityCryptographicOperations: JavaSecurityCryptographicOperations
) : KeyDecrypter {
    override fun decryptKey(encrypted: String): String {
        return encoder.encode(javaSecurityCryptographicOperations.decryptKeyWith(MasterKid(maserKid), encrypted.toByteArray()))
            .decodeToString()
    }
}