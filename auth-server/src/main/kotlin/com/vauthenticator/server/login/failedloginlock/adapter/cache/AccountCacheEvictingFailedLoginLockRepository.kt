package com.vauthenticator.server.login.failedloginlock.adapter.cache

import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockRepository
import java.time.Instant

/**
 * The Failed-Login Lock updates the account table directly, below the account cache, so the cached account is
 * evicted after each update (ADR-0012).
 */
class AccountCacheEvictingFailedLoginLockRepository(
    private val delegate: FailedLoginLockRepository,
    private val accountCacheOperation: CacheOperation<String, String>
) : FailedLoginLockRepository {

    override fun lockUntilIfUnlocked(userName: String, lockedUntil: Instant): Boolean =
        delegate.lockUntilIfUnlocked(userName, lockedUntil)
            .also { accountCacheOperation.evict(userName) }

    override fun releaseIfLockedUntil(userName: String, lockedUntil: Instant): Boolean =
        delegate.releaseIfLockedUntil(userName, lockedUntil)
            .also { accountCacheOperation.evict(userName) }
}
