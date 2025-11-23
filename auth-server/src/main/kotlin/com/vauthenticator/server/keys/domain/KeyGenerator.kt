package com.vauthenticator.server.keys.domain

interface KeyGenerator {
    fun dataKeyPairFor(masterKid: MasterKid): DataKey
    fun dataKeyFor(masterKid: MasterKid): DataKey
}

