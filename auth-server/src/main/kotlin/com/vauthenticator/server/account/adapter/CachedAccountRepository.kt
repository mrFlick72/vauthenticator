package com.vauthenticator.server.account.adapter

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation

class CachedAccountRepository(
    private val cacheContentConverter: CacheContentConverter<Account>,
    private val cacheOperation: CacheOperation<String, String>,
    private val delegate: AccountRepository
) : AccountRepository by delegate {

    override fun accountFor(username: String): Account? =
        cacheOperation.get(username)
            ?.let { cacheContentConverter.getObjectFromCacheContentFor(it) }
            ?: delegate.accountFor(username)?.also {
                cacheOperation.put(
                    username,
                    cacheContentConverter.loadableContentIntoCacheFor(it)
                )
            }

    override fun save(account: Account) {
        cacheOperation.evict(account.email)
        delegate.save(account)
    }

}
