package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.FailedLoginLockAppliedEvent
import com.vauthenticator.server.events.FailedLoginLockReleasedEvent
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ConfigurationProperties("login.failed-login-lock")
data class FailedLoginLockConfig(
    val threshold: Long = 5,
    val duration: Duration = Duration.ofMinutes(1)
)

enum class FailedLoginLockReleaseReason { EXPIRED, ADMIN_UNLOCK }

data class FailedLoginLockApplied(val lockedUntil: Instant)

data class FailedLoginLockRelease(val reason: FailedLoginLockReleaseReason, val lockedUntil: Instant)

interface ConsecutiveFailedLogins {
    fun increment(userName: String, window: Duration): Long
    fun reset(userName: String)
}

/**
 * Applies and lifts a Failed-Login Lock as conditional updates, so that concurrent logins, admin saves and lifecycle
 * locks never overwrite each other: each method returns true only when this call changed the account.
 */
interface FailedLoginLockStore {
    fun lockUntilIfUnlocked(userName: String, lockedUntil: Instant): Boolean
    fun releaseIfLockedUntil(userName: String, lockedUntil: Instant): Boolean
}

class FailedLoginLock(
    private val accountRepository: AccountRepository,
    private val failedLoginLockStore: FailedLoginLockStore,
    private val consecutiveFailedLogins: ConsecutiveFailedLogins,
    private val eventsDispatcher: EventsDispatcher,
    private val clock: Clock,
    private val config: FailedLoginLockConfig
) {

    fun failedLogin(userName: String, clientAppId: ClientAppId) {
        if (config.threshold <= 0) return
        val account = accountRepository.accountFor(userName) ?: return
        if (!account.accountNonLocked) return
        val failures = consecutiveFailedLogins.increment(userName, config.duration)
        if (failures >= config.threshold) {
            consecutiveFailedLogins.reset(userName)
            val now = clock.instant()
            // the store keeps microseconds: the applied event must carry the same lock end the release reads back
            val lockedUntil = now.plus(config.duration).truncatedTo(ChronoUnit.MICROS)
            if (failedLoginLockStore.lockUntilIfUnlocked(userName, lockedUntil)) {
                eventsDispatcher.dispatch(
                    FailedLoginLockAppliedEvent(Email(userName), clientAppId, now, FailedLoginLockApplied(lockedUntil))
                )
            }
        }
    }

    fun passwordProven(userName: String) {
        consecutiveFailedLogins.reset(userName)
    }

    fun releaseIfExpired(account: Account, clientAppId: ClientAppId): Account {
        val lockedUntil = account.lockedUntil ?: return account
        val now = clock.instant()
        if (now.isBefore(lockedUntil)) return account

        if (!failedLoginLockStore.releaseIfLockedUntil(account.username, lockedUntil)) {
            // another login released it first, or the lock changed meanwhile: the stored account decides
            return accountRepository.accountFor(account.username) ?: account
        }
        // failures counted while the lock was being applied must not carry into the next window
        consecutiveFailedLogins.reset(account.username)
        eventsDispatcher.dispatch(
            FailedLoginLockReleasedEvent(
                Email(account.username),
                clientAppId,
                now,
                FailedLoginLockRelease(FailedLoginLockReleaseReason.EXPIRED, lockedUntil)
            )
        )
        return account.unlock()
    }
}
