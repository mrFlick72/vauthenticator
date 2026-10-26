package com.vauthenticator.server.role.adapter.cache

import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.role.domain.GroupRepository

/**
 * Deleting a group cascades into the accounts that are in it, below the account cache, so the whole account cache is
 * cleared after a delete (ADR-0012).
 */
class AccountCacheEvictingGroupRepository(
    private val delegate: GroupRepository,
    private val accountCacheOperation: CacheOperation<String, String>
) : GroupRepository by delegate {

    override fun delete(groupName: String) {
        delegate.delete(groupName)
        accountCacheOperation.evictAll()
    }
}
