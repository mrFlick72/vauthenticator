package com.vauthenticator.server.keys.adapter.kms

import com.vauthenticator.server.keys.domain.DataKey
import com.vauthenticator.server.keys.domain.KeyGenerator
import com.vauthenticator.server.keys.domain.MasterKid
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.DataKeyPairSpec
import software.amazon.awssdk.services.kms.model.DataKeySpec
import software.amazon.awssdk.services.kms.model.GenerateDataKeyPairRequest
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest
import java.util.*

class KmsKeyGenerator(private val kmsClient: KmsClient) : KeyGenerator {

    override fun dataKeyPairFor(masterKid: MasterKid) = kmsClient.generateDataKeyPair(
        GenerateDataKeyPairRequest.builder()
            .keyId(masterKid.content())
            .keyPairSpec(DataKeyPairSpec.RSA_2048)
            .build()
    ).let {
        DataKey(
            encryptedPrivateKey = it.privateKeyCiphertextBlob().asByteArray(),
            publicKey = Optional.of(it.publicKey().asByteArray())
        )
    }

    override fun dataKeyFor(masterKid: MasterKid): DataKey = kmsClient.generateDataKey(
        GenerateDataKeyRequest.builder().keyId(masterKid.content()).keySpec(DataKeySpec.AES_256).build()
    ).let {
        DataKey(
            encryptedPrivateKey = it.ciphertextBlob().asByteArray(),
            publicKey = Optional.empty<ByteArray>()
        )
    }

}