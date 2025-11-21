package com.vauthenticator.server.management.init

import com.vauthenticator.server.keys.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KeySetUpJob(
    private val maserKid: String,
    private val keyStorage: KeyStorage,
    private val keyRepository: KeyRepository
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(KeySetUpJob::class.java)
    }

    fun execute() {

        if (keyStorage.signatureKeys().keys.isEmpty()) {
            val firstKid = keyRepository.createKeyFrom(
                masterKid = MasterKid(maserKid),
                keyPurpose = KeyPurpose.SIGNATURE,
                keyType = KeyType.ASYMMETRIC,
            )
            val secondKid = keyRepository.createKeyFrom(
                masterKid = MasterKid(maserKid),
                keyPurpose = KeyPurpose.SIGNATURE,
                keyType = KeyType.ASYMMETRIC,
            )
            logger.info("Token Signature Key init job has been executed. Key IDs generated: $firstKid and $secondKid")
        }

    }

}