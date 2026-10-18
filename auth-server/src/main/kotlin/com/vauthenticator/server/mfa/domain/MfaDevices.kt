package com.vauthenticator.server.mfa.domain

import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.MfaDeviceRevokedEvent
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.keys.domain.KeyPurpose
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * The account holder's management of their own MFA Devices. Revocation keeps a usable second factor
 * and is a hard delete (ADR 0009).
 */
class MfaDevices(
    private val mfaAccountMethodsRepository: MfaAccountMethodsRepository,
    private val keyRepository: KeyRepository,
    private val eventsDispatcher: VAuthenticatorEventsDispatcher
) {

    private val logger = LoggerFactory.getLogger(MfaDevices::class.java)

    fun setAsDefault(userName: String, mfaDeviceId: MfaDeviceId) {
        mfaAccountMethodsRepository.findBy(mfaDeviceId)
            ?.takeIf { it.userName == userName && it.associated }
            ?: throw MfaDeviceNotFoundException("no associated mfa device to set as default")
        mfaAccountMethodsRepository.setAsDefault(userName, mfaDeviceId)
    }

    fun revoke(userName: String, mfaDeviceId: MfaDeviceId, clientAppId: ClientAppId) {
        val revoked = mfaAccountMethodsRepository.revoke(userName, mfaDeviceId) { candidate ->
            // last device first: with no other device, "set another default" is advice the holder cannot follow
            if (candidate.device.associated && candidate.accountDevices.count { it.associated } <= 1) {
                throw MfaDeviceRevocationException(MfaDeviceRevocationRefusal.LAST_ASSOCIATED_DEVICE)
            }
            if (candidate.device.mfaDeviceId == candidate.defaultDevice) {
                throw MfaDeviceRevocationException(MfaDeviceRevocationRefusal.DEFAULT_DEVICE)
            }
        }

        deleteKeyOf(revoked)
        eventsDispatcher.dispatch(
            MfaDeviceRevokedEvent(
                Email(userName),
                clientAppId,
                Instant.now(),
                MfaDeviceRevoked(revoked.mfaDeviceId, revoked.mfaMethod)
            )
        )
    }

    private fun deleteKeyOf(revoked: MfaAccountMethod) {
        try {
            keyRepository.deleteKeyFor(revoked.key, KeyPurpose.MFA)
        } catch (e: RuntimeException) {
            // The device is already gone, so its key is orphaned: it can no longer verify any challenge.
            logger.error("the key ${revoked.key.content()} of the revoked mfa device ${revoked.mfaDeviceId.content} was not deleted", e)
        }
    }
}
