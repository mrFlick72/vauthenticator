package com.vauthenticator.server.keys.domain

import java.time.Duration

interface KeyStorage {
    fun store(masterKid: MasterKid, kid: Kid, dataKey: DataKey, keyType: KeyType, keyPurpose: KeyPurpose)

    fun signatureKeys(): Keys

    // todo would be better return an optional?
    fun findOne(kid: Kid, keyPurpose: KeyPurpose): Key

    fun justDeleteKey(kid: Kid, keyPurpose: KeyPurpose)

    fun keyDeleteJodPlannedFor(kid: Kid, ttl: Duration, keyPurpose: KeyPurpose)
}