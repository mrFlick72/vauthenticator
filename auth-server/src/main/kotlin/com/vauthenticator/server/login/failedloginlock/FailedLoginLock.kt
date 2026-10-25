package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.Email
import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.FailedLoginLockAppliedEvent
import com.vauthenticator.server.events.FailedLoginLockReleasedEvent
import com.vauthenticator.server.extentions.oauth2ClientId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Clock
import java.time.Duration
import java.time.Instant

@ConfigurationProperties("login.failed-login-lock")
data class FailedLoginLockConfig(
    val threshold: Long = 5,
    val duration: Duration = Duration.ofMinutes(1)
)

enum class FailedLoginLockReleaseReason { EXPIRED, ADMIN_UNLOCK }

data class FailedLoginLockApplied(val lockedUntil: Instant)

data class FailedLoginLockRelease(val reason: FailedLoginLockReleaseReason, val lockedUntil: Instant)

fun loginClientAppIdOf(request: HttpServletRequest): ClientAppId =
    request.oauth2ClientId() ?: request.getSession(false)?.oauth2ClientId() ?: ClientAppId.system()

interface ConsecutiveFailedLogins {
    fun increment(userName: String, window: Duration): Long
    fun reset(userName: String)
}

class FailedLoginLock(
    private val accountRepository: AccountRepository,
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
        if (failures == config.threshold) {
            consecutiveFailedLogins.reset(userName)
            val now = clock.instant()
            val lockedUntil = now.plus(config.duration)
            accountRepository.save(account.lockUntil(lockedUntil))
            eventsDispatcher.dispatch(
                FailedLoginLockAppliedEvent(Email(userName), clientAppId, now, FailedLoginLockApplied(lockedUntil))
            )
        }
    }

    fun passwordProven(userName: String) {
        consecutiveFailedLogins.reset(userName)
    }

    fun releaseIfExpired(account: Account, clientAppId: ClientAppId): Account {
        val lockedUntil = account.lockedUntil ?: return account
        val now = clock.instant()
        if (now.isBefore(lockedUntil)) return account

        val unlocked = account.unlock()
        accountRepository.save(unlocked)
        eventsDispatcher.dispatch(
            FailedLoginLockReleasedEvent(
                Email(account.username),
                clientAppId,
                now,
                FailedLoginLockRelease(FailedLoginLockReleaseReason.EXPIRED, lockedUntil)
            )
        )
        return unlocked
    }
}
