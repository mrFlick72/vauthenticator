package com.vauthenticator.server.keys.domain

import com.vauthenticator.server.extentions.asDynamoAttribute
import com.vauthenticator.server.extentions.encoder
import com.vauthenticator.server.extentions.valueAsStringFor
import com.vauthenticator.server.keys.adapter.dynamo.DynamoDbKeyStorage
import com.vauthenticator.server.keys.adapter.kms.KmsKeyGenerator
import com.vauthenticator.server.keys.domain.KeyPurpose.MFA
import com.vauthenticator.server.keys.domain.KeyPurpose.SIGNATURE
import com.vauthenticator.server.keys.domain.KeyType.ASYMMETRIC
import com.vauthenticator.server.keys.domain.KeyType.SYMMETRIC
import com.vauthenticator.server.support.DynamoDbUtils.dynamoDbClient
import com.vauthenticator.server.support.DynamoDbUtils.dynamoMfaKeysTableName
import com.vauthenticator.server.support.DynamoDbUtils.dynamoSignatureKeysTableName
import com.vauthenticator.server.support.DynamoDbUtils.resetDynamoDb
import com.vauthenticator.server.support.KeysUtils.aMasterKey
import com.vauthenticator.server.support.KeysUtils.aNewMasterKey
import com.vauthenticator.server.support.KeysUtils.aSignatureDataKey
import com.vauthenticator.server.support.KeysUtils.kmsClient
import com.vauthenticator.server.support.KmsClientWrapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
class KeyRepositoryTest {

    @MockK
    private lateinit var keyGenerator: KeyGenerator

    @MockK
    private lateinit var keyStorage: KeyStorage

    private lateinit var uut: KeyRepository

    private lateinit var keyRepository: KeyRepository
    private lateinit var wrapper: KmsClientWrapper

    private val kidGenerator = { "A_KID"}
    private val now = Instant.now()

    private val A_KID = Kid(kidGenerator.invoke())

    @BeforeEach
    fun setUp() {
        uut = KeyRepository(kidGenerator, keyStorage, keyGenerator)
        resetDynamoDb()


        wrapper = KmsClientWrapper(kmsClient)
        keyRepository =
            KeyRepository(
                kidGenerator,
                DynamoDbKeyStorage(
                    Clock.fixed(now, ZoneId.systemDefault()),
                    dynamoDbClient,
                    dynamoSignatureKeysTableName,
                    dynamoMfaKeysTableName
                ),
                KmsKeyGenerator(wrapper)
            )
    }

    @Test
    fun `when create a new data key pair`() {
        val masterKid = aMasterKey

        every { keyGenerator.dataKeyPairFor(masterKid) } returns aSignatureDataKey
        every { keyStorage.store(masterKid, A_KID, aSignatureDataKey, ASYMMETRIC, SIGNATURE) } just runs

        val kid = uut.createKeyFrom(masterKid, ASYMMETRIC)

        assertEquals(kid.content(), A_KID.content())
        verify { keyGenerator.dataKeyPairFor(masterKid) }
        verify { keyStorage.store(masterKid, A_KID, aSignatureDataKey, ASYMMETRIC, SIGNATURE) }
    }

    @Test
    fun `when create a new data key`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid, SYMMETRIC)

        val actual = getActual(kid, dynamoSignatureKeysTableName)
        assertEquals(kid.content(), actual.valueAsStringFor("key_id"))
        assertEquals(masterKid.content(), actual.valueAsStringFor("master_key_id"))
        assertEquals(
            encoder.encode(wrapper.generateDataKeyRecorder.get().ciphertextBlob().asByteArray())
                .decodeToString(), actual.valueAsStringFor("encrypted_private_key")
        )
    }

    @Test
    fun `when create a new data key for mfa`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid, SYMMETRIC, MFA)

        val actual = getActual(kid, dynamoMfaKeysTableName)
        assertEquals(kid.content(), actual.valueAsStringFor("key_id"))
        assertEquals(masterKid.content(), actual.valueAsStringFor("master_key_id"))
        assertEquals(
            encoder.encode(wrapper.generateDataKeyRecorder.get().ciphertextBlob().asByteArray())
                .decodeToString(), actual.valueAsStringFor("encrypted_private_key")
        )
    }

    @Test
    fun `when a signature key is deleted`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid)
        keyRepository.createKeyFrom(masterKid)
        val ttl = Duration.ofSeconds(1)
        keyRepository.deleteKeyFor(kid, SIGNATURE, ttl)

        val actual = getActual(kid, dynamoSignatureKeysTableName)
        val expectedTTl = now.epochSecond + 1
        assertEquals(false, (actual["enabled"] as AttributeValue).bool())
        assertEquals(expectedTTl, (actual["key_expiration_date_timestamp"] as AttributeValue).n().toLong())
    }

    @Test
    fun `when delete a rotated signature key`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid)
        keyRepository.createKeyFrom(masterKid)
        keyRepository.createKeyFrom(masterKid)
        keyRepository.createKeyFrom(masterKid)
        keyRepository.createKeyFrom(masterKid)
        val ttl = Duration.ofSeconds(1)
        keyRepository.deleteKeyFor(kid, SIGNATURE, ttl)

        val actual = getActual(kid, dynamoSignatureKeysTableName)
        val expectedTTl = now.epochSecond + 1
        assertEquals(false, (actual["enabled"] as AttributeValue).bool())
        assertEquals(expectedTTl, (actual["key_expiration_date_timestamp"] as AttributeValue).n().toLong())

        assertThrows(KeyDeletionException::class.java) {
            keyRepository.deleteKeyFor(kid, SIGNATURE)
        }
    }

    @Test
    fun `when a signature key deleted request is ignored due to only one valid key is left`() {
        val masterKid = aNewMasterKey()
        val firstKid = keyRepository.createKeyFrom(masterKid)
        val secondKid = keyRepository.createKeyFrom(masterKid)

        val ttl = Duration.ofSeconds(10)
        keyRepository.deleteKeyFor(firstKid, SIGNATURE, ttl)

        assertThrows(KeyDeletionException::class.java) {
            keyRepository.deleteKeyFor(secondKid, SIGNATURE)
        }

    }

    @Test
    fun `when a signature key deletion goes in error`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid)

        assertThrows(KeyDeletionException::class.java) { keyRepository.deleteKeyFor(kid, SIGNATURE) }
    }

    @Test
    fun `when a mfa key is deleted`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid = masterKid, keyPurpose = MFA)
        keyRepository.deleteKeyFor(kid, MFA)

        val actual = getActual(kid, dynamoMfaKeysTableName)
        assertEquals(emptyMap<String, AttributeValue>(), actual)
    }


    private fun getActual(kid: Kid, tableName: String): MutableMap<String, AttributeValue> =
        dynamoDbClient.getItem(
            GetItemRequest.builder().tableName(tableName)
                .key(
                    mapOf(
                        "key_id" to kid.content().asDynamoAttribute()
                    )
                )
                .build()
        ).item()

    @Test
    fun `when a key is found by id and purpose`() {
        val masterKid = aNewMasterKey()
        val kid = keyRepository.createKeyFrom(masterKid, SYMMETRIC, MFA)

        val actual = keyRepository.keyFor(kid, MFA)
        val expected = Key(
            DataKey.from(
                encoder.encode(wrapper.generateDataKeyRecorder.get().ciphertextBlob().asByteArray())
                    .decodeToString(),
                ""
            ),
            masterKid,
            kid,
            true,
            SYMMETRIC,
            MFA,
            0L
        )

        assertEquals(expected, actual)
    }

}