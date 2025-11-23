package com.vauthenticator.server.mfa.adapter

import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.mfa.domain.MfaAccountMethod
import com.vauthenticator.server.mfa.domain.MfaAccountMethodsRepository
import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.mfa.domain.MfaMethod
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

abstract class AbstractMfaAccountMethodsRepositoryTest {

    val mfaDeviceId = MfaDeviceId("A_MFA_DEVICE_ID")
    val masterKid = MasterKid("")
    val email = anAccount().email
    val key = Kid("")

    @MockK
    lateinit var keyRepository: KeyRepository

    lateinit var uut: MfaAccountMethodsRepository
    abstract fun initMfaAccountMethodsRepository(): MfaAccountMethodsRepository
    abstract fun resetDatabase()

    @BeforeEach
    fun setUp() {
        every { keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA) } returns key

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
    fun `when try to get one specific enrolment association`() {
        every { keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA) } returns Kid("")

        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val mfaAccountMethods = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email)
        assertEquals(
            Optional.of(MfaAccountMethod(email, mfaDeviceId, key, MfaMethod.EMAIL_MFA_METHOD, email, true)),
            mfaAccountMethods
        )
    }

    @Test
    fun `when one specific mfa account method is found by device id`() {
        every { keyRepository.createKeyFrom(masterKid, KeyType.SYMMETRIC, KeyPurpose.MFA) } returns Kid("")

        val savedMfaAccountMethod = uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val mfaAccountMethods = uut.findBy(savedMfaAccountMethod.mfaDeviceId)

        assertEquals(
            Optional.of(MfaAccountMethod(email, mfaDeviceId, key, MfaMethod.EMAIL_MFA_METHOD, email, true)),
            mfaAccountMethods
        )

    }

    @Test
    fun `when one specific enrolment association is not found`() {
        val mfaAccountMethods = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email)
        val expected = Optional.empty<Any>()
        assertEquals(expected, mfaAccountMethods)
    }

    @Test
    fun `when decide what mfa use as default`() {
        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)

        val expected = Optional.of(mfaDeviceId)
        uut.setAsDefault(email, mfaDeviceId)
        val defaultDevice = uut.getDefaultDevice(email)

        assertEquals(expected, defaultDevice)
    }

    @Test
    fun `when a mfa account method is stored and then enabled`() {
        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, false)
        val beforeToBeAssociated = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email).get()

        uut.save(email, MfaMethod.EMAIL_MFA_METHOD, email, true)
        val afterAssociated = uut.findBy(email, MfaMethod.EMAIL_MFA_METHOD, email).get()


        assertEquals(afterAssociated.mfaDeviceId, beforeToBeAssociated.mfaDeviceId)
        assertEquals(afterAssociated.mfaChannel, beforeToBeAssociated.mfaChannel)
        assertEquals(afterAssociated.mfaMethod, beforeToBeAssociated.mfaMethod)
        assertEquals(beforeToBeAssociated.associated, false)
        assertEquals(afterAssociated.associated, true)
        assertEquals(afterAssociated.key, beforeToBeAssociated.key)
        assertEquals(afterAssociated.userName, beforeToBeAssociated.userName)
    }

}