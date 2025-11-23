package com.vauthenticator.server.keys.adapter

import com.vauthenticator.server.extentions.encoder
import com.vauthenticator.server.keys.domain.KeyPurpose
import com.vauthenticator.server.keys.domain.KeyPurpose.MFA
import com.vauthenticator.server.keys.domain.KeyPurpose.SIGNATURE
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.keys.domain.KeyType.ASYMMETRIC
import com.vauthenticator.server.keys.domain.KeyType.SYMMETRIC
import com.vauthenticator.server.keys.domain.Keys
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.support.KeysUtils.aKeyFor
import com.vauthenticator.server.support.KeysUtils.aKid
import com.vauthenticator.server.support.KeysUtils.aMasterKey
import com.vauthenticator.server.support.KeysUtils.aSignatureDataKey
import com.vauthenticator.server.support.KeysUtils.aSimmetricDataKey
import com.vauthenticator.server.support.KeysUtils.anotherKid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlin.test.assertNotNull

abstract class
AbstractKeyStorageTest {

    private val masterKid = aMasterKey
    private val now = Instant.now()

    private lateinit var uut: KeyStorage

    abstract fun initKeyStorage(): KeyStorage
    abstract fun resetDatabase()
    abstract fun getActual(kid: Kid, keyPurpose: KeyPurpose): Map<String, Any>

    fun clock(): Clock = Clock.fixed(now, ZoneId.systemDefault())

    @BeforeEach
    fun setUp() {
        resetDatabase()
        uut = initKeyStorage()
    }

    @Test
    fun `when store a new data key pair`() {
        uut.store(masterKid, aKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)

        val actual = getActual(aKid, SIGNATURE)
        assertEquals(aKid.content(), actual["key_id"])
        assertEquals(masterKid.content(), actual["master_key_id"])
        assertEquals(
            encoder.encode(aSignatureDataKey.encryptedPrivateKey)
                .decodeToString(), actual["encrypted_private_key"]
        )
        assertEquals(
            encoder.encode(aSignatureDataKey.publicKey.get()).decodeToString(),
            actual["public_key"]
        )
    }

    @Test
    fun `when store a new data key for mfa`() {
        uut.store(masterKid, aKid, aSimmetricDataKey, SYMMETRIC, MFA)

        val actual = getActual(aKid, MFA)
        assertEquals(aKid.content(), actual["key_id"])
        assertEquals(masterKid.content(), actual["master_key_id"])
        assertEquals(
            encoder.encode(aSimmetricDataKey.encryptedPrivateKey)
                .decodeToString(), actual["encrypted_private_key"]
        )
    }

    @Test
    fun `when find an mfa key`() {
        uut.store(masterKid, aKid, aSimmetricDataKey, SYMMETRIC, MFA)
        val actual = uut.findOne(aKid, MFA)
        val excpected = aKeyFor(masterKid.content(), aKid.content(), SYMMETRIC, MFA)
        assertEquals(excpected, actual)
    }

    @Test
    fun `when find an signature key`() {
        uut.store(masterKid, aKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)
        val actual = uut.findOne(aKid, SIGNATURE)
        val excpected = aKeyFor(masterKid.content(), aKid.content(), ASYMMETRIC, SIGNATURE)
        assertEquals(excpected, actual)
    }

    @Test
    fun `when find all signature keys`() {
        uut.store(masterKid, aKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)
        uut.store(masterKid, anotherKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)
        val actual = uut.signatureKeys()
        val excpected = Keys(
            listOf(
                aKeyFor(masterKid.content(), aKid.content(), ASYMMETRIC, SIGNATURE),
                aKeyFor(masterKid.content(), anotherKid.content(), ASYMMETRIC, SIGNATURE)
            )
        )
        assertEquals(excpected, actual)
    }

    @Test
    fun `when a signature key is deleted`() {
        uut.store(masterKid, aKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)
        val storedKey = uut.findOne(aKid, SIGNATURE)
        assertNotNull(storedKey)

        uut.justDeleteKey(aKid, SIGNATURE)
        val actual = getActual(aKid, SIGNATURE)
        assertEquals(emptyMap<String, Any>(), actual)
    }

    @Test
    fun `when a signature key planned  to be deleted`() {
        uut.store(masterKid, aKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)
        val storedKey = uut.findOne(aKid, SIGNATURE)
        assertNotNull(storedKey)

        val ttl = Duration.ofSeconds(1)
        uut.keyDeleteJodPlannedFor(aKid, ttl, SIGNATURE)

        val actual = getActual(aKid, SIGNATURE)
        val expectedTTl = now.epochSecond + 1
        assertEquals(false, (actual["enabled"] as Boolean))
        assertEquals(expectedTTl, (actual["key_expiration_date_timestamp"] as Long))
    }

    @Test
    fun `when a signature key deletion planning is ignored`() {
        uut.store(masterKid, aKid, aSignatureDataKey, ASYMMETRIC, SIGNATURE)
        val storedKey = uut.findOne(aKid, SIGNATURE)
        assertNotNull(storedKey)

        uut.keyDeleteJodPlannedFor(aKid, Duration.ofSeconds(1), SIGNATURE)

        val actual = getActual(aKid, SIGNATURE)
        val expectedTTl = now.epochSecond + 1
        assertEquals(false, (actual["enabled"] as Boolean))
        assertEquals(expectedTTl, (actual["key_expiration_date_timestamp"] as Long))

        uut.keyDeleteJodPlannedFor(aKid, Duration.ofSeconds(10), SIGNATURE)

        val actualAfterReplanning = getActual(aKid, SIGNATURE)
        val expectedTTlAfterReplanning = now.epochSecond + 1
        assertEquals(false, (actualAfterReplanning["enabled"] as Boolean))
        assertEquals(expectedTTlAfterReplanning, (actualAfterReplanning["key_expiration_date_timestamp"] as Long))
    }

}