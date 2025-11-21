package com.vauthenticator.server.keys.domain

fun interface KeyDecrypter {
    fun decryptKey(encrypted: String): String
}

