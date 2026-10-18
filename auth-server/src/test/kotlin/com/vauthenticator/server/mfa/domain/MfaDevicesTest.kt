package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.MfaDeviceRevokedEvent
import com.vauthenticator.server.events.VAuthenticatorEvent
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.keys.domain.KeyPurpose
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.Kid
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class MfaDevicesTest {

    private val userName = "user@email.com"
    private val clientAppId = ClientAppId("A_CLIENT_APP_ID")

    private val associatedDevice = device("ASSOCIATED_DEVICE_ID", associated = true)
    private val anotherAssociatedDevice = device("ANOTHER_ASSOCIATED_DEVICE_ID", associated = true)
    private val enrolledDevice = device("ENROLLED_DEVICE_ID", associated = false)

    @MockK
    lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    @MockK
    lateinit var keyRepository: KeyRepository

    @MockK
    lateinit var eventsDispatcher: VAuthenticatorEventsDispatcher

    lateinit var underTest: MfaDevices

    @BeforeEach
    fun setUp() {
        underTest = MfaDevices(mfaAccountMethodsRepository, keyRepository, eventsDispatcher)
        every { keyRepository.deleteKeyFor(any(), KeyPurpose.MFA) } just runs
        every { eventsDispatcher.dispatch(any()) } just runs
    }

    @Test
    fun `when a mfa device is revoked its key is deleted and the revocation is published`() {
        givenRevocationOf(associatedDevice, listOf(associatedDevice, anotherAssociatedDevice), anotherAssociatedDevice.mfaDeviceId)
        val event = slot<VAuthenticatorEvent>()
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        underTest.revoke(userName, associatedDevice.mfaDeviceId, clientAppId)

        verify { keyRepository.deleteKeyFor(associatedDevice.key, KeyPurpose.MFA) }
        val revokedEvent = event.captured as MfaDeviceRevokedEvent
        assertEquals(Email(userName), revokedEvent.userName)
        assertEquals(clientAppId, revokedEvent.clientAppId)
        assertEquals(MfaDeviceRevoked(associatedDevice.mfaDeviceId, MfaMethod.EMAIL_MFA_METHOD), revokedEvent.payload)
    }

    @Test
    fun `when the default mfa device is revoked the revocation is refused`() {
        givenRevocationOf(associatedDevice, listOf(associatedDevice, anotherAssociatedDevice), associatedDevice.mfaDeviceId)

        val refusal = assertThrows(MfaDeviceRevocationException::class.java) {
            underTest.revoke(userName, associatedDevice.mfaDeviceId, clientAppId)
        }

        assertEquals(MfaDeviceRevocationRefusal.DEFAULT_DEVICE, refusal.refusal)
        verify(exactly = 0) { keyRepository.deleteKeyFor(any(), any()) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when the last associated mfa device is revoked the revocation is refused`() {
        givenRevocationOf(associatedDevice, listOf(associatedDevice, enrolledDevice), null)

        val refusal = assertThrows(MfaDeviceRevocationException::class.java) {
            underTest.revoke(userName, associatedDevice.mfaDeviceId, clientAppId)
        }

        assertEquals(MfaDeviceRevocationRefusal.LAST_ASSOCIATED_DEVICE, refusal.refusal)
        verify(exactly = 0) { keyRepository.deleteKeyFor(any(), any()) }
    }

    @Test
    fun `when the only mfa device is also the default the revocation is refused as the last associated device`() {
        givenRevocationOf(associatedDevice, listOf(associatedDevice), associatedDevice.mfaDeviceId)

        val refusal = assertThrows(MfaDeviceRevocationException::class.java) {
            underTest.revoke(userName, associatedDevice.mfaDeviceId, clientAppId)
        }

        assertEquals(MfaDeviceRevocationRefusal.LAST_ASSOCIATED_DEVICE, refusal.refusal)
    }

    @Test
    fun `when an enrolled mfa device is revoked it is revoked even without associated devices`() {
        givenRevocationOf(enrolledDevice, listOf(enrolledDevice), null)

        underTest.revoke(userName, enrolledDevice.mfaDeviceId, clientAppId)

        verify { keyRepository.deleteKeyFor(enrolledDevice.key, KeyPurpose.MFA) }
        verify { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when the key of a revoked mfa device cannot be deleted the revocation is still published`() {
        givenRevocationOf(associatedDevice, listOf(associatedDevice, anotherAssociatedDevice), anotherAssociatedDevice.mfaDeviceId)
        every { keyRepository.deleteKeyFor(associatedDevice.key, KeyPurpose.MFA) } throws RuntimeException("key storage down")

        underTest.revoke(userName, associatedDevice.mfaDeviceId, clientAppId)

        verify { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when an associated mfa device of the account is set as default`() {
        every { mfaAccountMethodsRepository.findBy(associatedDevice.mfaDeviceId) } returns associatedDevice
        every { mfaAccountMethodsRepository.setAsDefault(userName, associatedDevice.mfaDeviceId) } just runs

        underTest.setAsDefault(userName, associatedDevice.mfaDeviceId)

        verify { mfaAccountMethodsRepository.setAsDefault(userName, associatedDevice.mfaDeviceId) }
    }

    @Test
    fun `when an enrolled mfa device is set as default it is not found`() {
        every { mfaAccountMethodsRepository.findBy(enrolledDevice.mfaDeviceId) } returns enrolledDevice

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.setAsDefault(userName, enrolledDevice.mfaDeviceId)
        }

        verify(exactly = 0) { mfaAccountMethodsRepository.setAsDefault(any(), any()) }
    }

    @Test
    fun `when a mfa device of another account is set as default it is not found`() {
        every { mfaAccountMethodsRepository.findBy(associatedDevice.mfaDeviceId) } returns
                associatedDevice.copy(userName = "another@email.com")

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.setAsDefault(userName, associatedDevice.mfaDeviceId)
        }

        verify(exactly = 0) { mfaAccountMethodsRepository.setAsDefault(any(), any()) }
    }

    @Test
    fun `when an unknown mfa device is set as default it is not found`() {
        every { mfaAccountMethodsRepository.findBy(associatedDevice.mfaDeviceId) } returns null

        assertThrows(MfaDeviceNotFoundException::class.java) {
            underTest.setAsDefault(userName, associatedDevice.mfaDeviceId)
        }

        verify(exactly = 0) { mfaAccountMethodsRepository.setAsDefault(any(), any()) }
    }

    private fun givenRevocationOf(
        device: MfaAccountMethod,
        accountDevices: List<MfaAccountMethod>,
        defaultDevice: MfaDeviceId?
    ) {
        val check = slot<MfaDeviceRevocationCheck>()
        every { mfaAccountMethodsRepository.revoke(userName, device.mfaDeviceId, capture(check)) } answers {
            check.captured(MfaDeviceRevocationCandidate(device, accountDevices, defaultDevice))
            device
        }
    }

    private fun device(id: String, associated: Boolean) =
        MfaAccountMethod(userName, MfaDeviceId(id), Kid("KID_$id"), MfaMethod.EMAIL_MFA_METHOD, "$id@email.com", associated)
}
