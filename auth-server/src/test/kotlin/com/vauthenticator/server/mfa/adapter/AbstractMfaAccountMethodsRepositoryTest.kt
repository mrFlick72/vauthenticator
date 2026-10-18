package com.vauthenticator.server.mfa.adapter

import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.util.*

abstract class AbstractMfaAccountMethodsRepositoryTest {

    val mfaDeviceId = MfaDeviceId("A_MFA_DEVICE_ID")
    val masterKid = MasterKid("")
    val email = anAccount().email
    val key = Kid("")

    @MockK
    lateinit var keyRepository: KeyRepository

    private var generatedDeviceIds = 0
    val mfaDeviceIdGenerator: () -> MfaDeviceId = {
        generatedDeviceIds++
        if (generatedDeviceIds == 1) mfaDeviceId else MfaDeviceId("A_MFA_DEVICE_ID_$generatedDeviceIds")
    }

    lateinit var uut: MfaAccountMethodsRepository
    abstract fun initMfaAccountMethodsRepository(): MfaAccountMethodsRepository
    abstract fun resetDatabase()

    @BeforeEach
    fun setUp() {
        every { keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA) } returns key

        generatedDeviceIds = 0
        resetDatabase()
        uut = initMfaAccountMethodsRepository()
    }

    @Test
    fun `when a mfa account method is stored`() {
        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val mfaAccountMethods = uut.findAll(email)
        assertEquals(
            listOf(MfaAccountMethod(email, mfaDeviceId, key, MfaMethod.EMAIL_MFA_METHOD, email, true)),
            mfaAccountMethods
        )
    }

    @Test
    fun `when a mfa account do not have method stored`() {
        val mfaAccountMethods = uut.findAll(email)
        assertEquals(emptyList<MfaAccountMethod>(), mfaAccountMethods)
    }

    @Test
    fun `findAll does not leak other accounts' mfa methods`() {
        val otherEmail = "other-$email"

        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        uut.save(otherEmail, MfaMethod.EMAIL_MFA_METHOD, otherEmail, true)

        val mfaAccountMethods = uut.findAll(email)

        assertEquals(
            listOf(MfaAccountMethod(email, mfaDeviceId, key, MfaMethod.EMAIL_MFA_METHOD, email, true)),
            mfaAccountMethods
        )
    }

    @Test
    fun `when try to get one specific enrolment association`() {
        every { keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA) } returns Kid("")

        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val mfaAccountMethods = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email)
        assertEquals(
            MfaAccountMethod(email, mfaDeviceId, key, MfaMethod.EMAIL_MFA_METHOD, email, true),
            mfaAccountMethods
        )
    }

    @Test
    fun `when one specific mfa account method is found by device id`() {
        every { keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA) } returns Kid("")

        val savedMfaAccountMethod = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val mfaAccountMethods = uut.findBy(savedMfaAccountMethod.mfaDeviceId)

        assertEquals(
            MfaAccountMethod(email, mfaDeviceId, key, MfaMethod.EMAIL_MFA_METHOD, email, true),
            mfaAccountMethods
        )

    }

    @Test
    fun `when one specific enrolment association is not found`() {
        val mfaAccountMethods = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email)
        assertNull(mfaAccountMethods)
    }

    @Test
    fun `when decide what mfa use as default`() {
        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)

        val expected = mfaDeviceId
        uut.setAsDefault(email, mfaDeviceId)
        val defaultDevice = uut.getDefaultDevice(email)

        assertEquals(expected, defaultDevice)
    }

    @Test
    fun `when a mfa account method is stored and then enabled`() {
        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, false)
        val beforeToBeAssociated = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email)

        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val afterAssociated = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email)


        assertEquals(afterAssociated?.mfaDeviceId, beforeToBeAssociated?.mfaDeviceId)
        assertEquals(afterAssociated?.mfaChannel, beforeToBeAssociated?.mfaChannel)
        assertEquals(afterAssociated?.mfaMethod, beforeToBeAssociated?.mfaMethod)
        assertEquals(beforeToBeAssociated?.associated, false)
        assertEquals(afterAssociated?.associated, true)
        assertEquals(afterAssociated?.key, beforeToBeAssociated?.key)
        assertEquals(afterAssociated?.userName, beforeToBeAssociated?.userName)
    }

    @Test
    fun `when a mfa device is revoked it is removed and the other devices of the account are kept`() {
        val revoked = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val kept = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, "another-$email", true)
        uut.setAsDefault(email, kept.mfaDeviceId)
        var candidate: MfaDeviceRevocationCandidate? = null

        val actual = uut.revoke(email, revoked.mfaDeviceId) { candidate = it }

        assertEquals(revoked, actual)
        assertEquals(MfaDeviceRevocationCandidate(revoked, listOf(revoked, kept), kept.mfaDeviceId), candidate?.sorted())
        assertNull(uut.findBy(revoked.mfaDeviceId))
        assertEquals(listOf(kept), uut.findAll(email))
        assertEquals(kept.mfaDeviceId, uut.getDefaultDevice(email))
    }

    @Test
    fun `when a mfa device revocation check fails the device is kept`() {
        val device = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)

        assertThrows(MfaDeviceRevocationException::class.java) {
            uut.revoke(email, device.mfaDeviceId) {
                throw MfaDeviceRevocationException(MfaDeviceRevocationRefusal.LAST_ASSOCIATED_DEVICE)
            }
        }

        assertEquals(listOf(device), uut.findAll(email))
    }

    @Test
    fun `when an unknown mfa device is revoked`() {
        var checked = false

        assertThrows(MfaDeviceNotFoundException::class.java) {
            uut.revoke(email, MfaDeviceId("AN_UNKNOWN_MFA_DEVICE_ID")) { checked = true }
        }

        assertEquals(false, checked)
    }

    @Test
    fun `when a mfa device of another account is revoked it is not found and kept`() {
        val otherEmail = "other-$email"
        val otherAccountDevice = uut.save(otherEmail, MfaMethod.EMAIL_MFA_METHOD, otherEmail, true)
        var checked = false

        assertThrows(MfaDeviceNotFoundException::class.java) {
            uut.revoke(email, otherAccountDevice.mfaDeviceId) { checked = true }
        }

        assertEquals(false, checked)
        assertEquals(listOf(otherAccountDevice), uut.findAll(otherEmail))
    }

    private fun MfaDeviceRevocationCandidate.sorted() =
        copy(accountDevices = accountDevices.sortedBy { it.mfaDeviceId.content })
}
