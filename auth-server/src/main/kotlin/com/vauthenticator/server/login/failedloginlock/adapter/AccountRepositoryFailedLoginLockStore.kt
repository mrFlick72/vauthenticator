package com.vauthenticator.server.login.failedloginlock.adapter

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockStore
import java.time.Instant

/**
 * Read-then-save fallback for the `dynamo` profile, which has no conditional update and does not store the lock end
 * (there a Failed-Login Lock is lasting). DynamoDB support is being removed; the `database` profile uses
 * [com.vauthenticator.server.login.failedloginlock.adapter.jdbc.JdbcFailedLoginLockStore].
 */
class AccountRepositoryFailedLoginLockStore(private val accountRepository: AccountRepository) : FailedLoginLockStore {

    override fun lockUntilIfUnlocked(userName: String, lockedUntil: Instant): Boolean {
        val account = accountRepository.accountFor(userName)?.takeIf { it.accountNonLocked } ?: return false
        accountRepository.save(account.lockUntil(lockedUntil))
        return true
    }

    override fun releaseIfLockedUntil(userName: String, lockedUntil: Instant): Boolean {
        val account = accountRepository.accountFor(userName)?.takeIf { it.lockedUntil == lockedUntil } ?: return false
        accountRepository.save(account.unlock())
        return true
    }
}
