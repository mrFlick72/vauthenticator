package com.vauthenticator.server.support

import com.vauthenticator.server.keys.domain.*
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.*
import java.net.URI
import java.util.*


object KeysUtils {

    val aSignatureDataKey: DataKey = DataKey(ByteArray(2323), Optional.of(ByteArray(23)))
    val aSimmetricDataKey: DataKey = DataKey(ByteArray(0), Optional.empty())

    val aKid: Kid = Kid("A_KID")
    val anotherKid: Kid = Kid("ANOTHER_KID")
    val aMasterKey: MasterKid = MasterKid("A_MASTER_KEY")

    private val policy = """
        {
                "Id": "key-policy",
                "Version": "2012-10-17",
                "Statement": [
                {
                    "Sid": "Enable IAM User Permissions",
                    "Effect": "Allow",
                    "Principal": {
                    "AWS": "arn:aws:iam::000000000000:ACCESS_KEY_ID"
                },
                    "Action": "kms:*",
                    "Resource": "*"
                }
            }
            ]
        }
    """.trimIndent()

    val kmsClient: KmsClient = KmsClient.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create("ACCESS_KEY_ID", "SECRET_ACCESS_KEY")
            )
        ).region(Region.US_EAST_1)
        .endpointOverride(URI.create("http://localhost:4566"))
        .build()

    fun aNewMasterKey(): MasterKid = MasterKid(
        kmsClient.createKey(
            CreateKeyRequest.builder()
                .keySpec("SYMMETRIC_DEFAULT")
                .description("A_KEY_DESCRIPTION")
                .keyUsage(KeyUsageType.ENCRYPT_DECRYPT)
                .policy(policy)
                .build()
        ).keyMetadata().keyId()
    )

    fun aKeyFor(
        masterKey: String,
        kid: String,
        keyType: KeyType = KeyType.ASYMMETRIC,
        keyPurpose: KeyPurpose = KeyPurpose.SIGNATURE
    ) = Key(
        if(keyType == KeyType.ASYMMETRIC) {
            aSignatureDataKey} else {
            aSimmetricDataKey},
        MasterKid(masterKey),
        Kid(kid),
        true,
        keyType,
        keyPurpose,
        0L
    )
}

class KmsClientWrapper(
    private val kmsClient: KmsClient,
    var generateDataKeyPairRecorder: Optional<GenerateDataKeyPairResponse> = Optional.empty(),
    var generateDataKeyRecorder: Optional<GenerateDataKeyResponse> = Optional.empty()
) : KmsClient by kmsClient {

    override fun generateDataKeyPair(request: GenerateDataKeyPairRequest): GenerateDataKeyPairResponse {
        val generateDataKeyPair = kmsClient.generateDataKeyPair(request)
        generateDataKeyPairRecorder = Optional.of(generateDataKeyPair)
        return generateDataKeyPair
    }

    override fun generateDataKey(request: GenerateDataKeyRequest): GenerateDataKeyResponse {
        val generateDataKey = kmsClient.generateDataKey(request)
        generateDataKeyRecorder = Optional.of(generateDataKey)
        return generateDataKey
    }


}