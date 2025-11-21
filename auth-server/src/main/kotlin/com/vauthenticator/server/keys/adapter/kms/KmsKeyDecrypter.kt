package com.vauthenticator.server.keys.adapter.kms

import com.vauthenticator.server.extentions.decoder
import com.vauthenticator.server.extentions.encoder
import com.vauthenticator.server.keys.domain.KeyDecrypter
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.DecryptRequest

class KmsKeyDecrypter(private val kmsClient: KmsClient) : KeyDecrypter {
    override fun decryptKey(privateKey: String): String = kmsClient.decrypt(
        DecryptRequest.builder()
            .ciphertextBlob(SdkBytes.fromByteArray(decoder.decode(privateKey)))
            .build()
    ).let {
        encoder.encode(it.plaintext().asByteArray()).decodeToString()
    }
}