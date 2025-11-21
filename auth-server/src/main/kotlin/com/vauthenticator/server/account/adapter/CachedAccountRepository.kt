package com.vauthenticator.server.account.adapter

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.cache.CacheContentConverter
import com.vauthenticator.server.cache.CacheOperation
import java.util.*
import java.util.Optional.of

class CachedAccountRepository(
    private val cacheContentConverter: CacheContentConverter<Account>,
    private val cacheOperation: CacheOperation<String, String>,
    private val delegate: AccountRepository
) : AccountRepository by delegate {

    override fun accountFor(username: String): Optional<Account> {
        return cacheOperation.get(username)
            .flatMap { of(cacheContentConverter.getObjectFromCacheContentFor(it)) }
            .or {
                val loadedAccount = delegate.accountFor(username)
                loadedAccount.ifPresent {
                    cacheOperation.put(
                        username,
                        cacheContentConverter.loadableContentIntoCacheFor(it)
                    )
                }
                loadedAccount
            }
    }

    override fun save(account: Account) {
        cacheOperation.evict(account.email)
        delegate.save(account)
    }

}