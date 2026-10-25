package com.vauthenticator.server.account.domain

import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.FailedLoginLockReleasedEvent
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockRelease
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockReleaseReason
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import java.time.Clock


data class AdminAccountApiRequest(
    val accountLocked: Boolean = true,
    val enabled: Boolean = true,
    var email: String = "",
    val authorities: Set<String> = setOf(),
    val mandatoryAction: AccountMandatoryAction = AccountMandatoryAction.NO_ACTION
)


class AccountUpdateAdminAction(
    private val accountRepository: AccountRepository,
    private val eventsDispatcher: EventsDispatcher,
    private val clock: Clock
) {
    fun execute(
        request: AdminAccountApiRequest,
        clientAppId: ClientAppId
    ) {
        accountRepository.accountFor(request.email)?.let { account ->
            accountRepository.save(
                lockStateFor(account, request.accountLocked).copy(
                    enabled = request.enabled,
                    authorities = request.authorities,
                    mandatoryAction = request.mandatoryAction
                )
            )
            releasedFailedLoginLock(account, request.accountLocked, clientAppId)
        }
    }

    // the admin always sends the whole account: only a change of the lock state is an explicit lock or unlock
    private fun lockStateFor(account: Account, locked: Boolean) =
        when {
            locked == !account.accountNonLocked -> account
            locked -> account.lock()
            else -> account.unlock()
        }

    private fun releasedFailedLoginLock(account: Account, locked: Boolean, clientAppId: ClientAppId) {
        val lockedUntil = account.lockedUntil
        if (!locked && lockedUntil != null) {
            eventsDispatcher.dispatch(
                FailedLoginLockReleasedEvent(
                    Email(account.username),
                    clientAppId,
                    clock.instant(),
                    FailedLoginLockRelease(FailedLoginLockReleaseReason.ADMIN_UNLOCK, lockedUntil)
                )
            )
        }
    }

}
