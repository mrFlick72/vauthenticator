package com.vauthenticator.server.role.adapter.cache

import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.role.domain.RoleRepository

/**
 * Deleting a role cascades into the accounts that hold it, below the account cache, so the whole account cache is
 * cleared after a delete (ADR-0012).
 */
class AccountCacheEvictingRoleRepository(
    private val delegate: RoleRepository,
    private val accountCacheOperation: CacheOperation<String, String>
) : RoleRepository by delegate {

    override fun delete(role: String) {
        delegate.delete(role)
        accountCacheOperation.evictAll()
    }
}
