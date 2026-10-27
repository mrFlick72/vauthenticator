package com.vauthenticator.server.login.failedloginlock.adapter.cache

import com.vauthenticator.server.account.adapter.CachedAccountRepository
import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.cache.RedisCacheOperation
import com.vauthenticator.server.login.failedloginlock.adapter.jdbc.JdbcFailedLoginLockRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.RedisUtils.aRedisTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.Duration
import java.time.Instant

private val LOCKED_UNTIL: Instant = Instant.parse("2026-10-25T10:16:30Z")

class AccountCacheEvictingFailedLoginLockRepositoryTest {

    private val accountCache = RedisCacheOperation<String, String>("account_cache_test", Duration.ofMinutes(1), aRedisTemplate())
    private val accountRepository = CachedAccountRepository(
        AccountCacheContentConverter(ObjectMapper()),
        accountCache,
        JdbcAccountRepository(jdbcTemplate)
    )
    private val uut = AccountCacheEvictingFailedLoginLockRepository(JdbcFailedLoginLockRepository(jdbcTemplate), accountCache)
    private val account = anAccount()

    @BeforeEach
    fun setUp() {
        resetDb()
        accountCache.evictAll()
    }

    @Test
    fun `when a cached account gets a failed-login lock it is read locked`() {
        accountRepository.create(account.unlock())
        accountRepository.accountFor(account.username)

        uut.lockUntilIfUnlocked(account.username, LOCKED_UNTIL)

        assertEquals(account.lockUntil(LOCKED_UNTIL), accountRepository.accountFor(account.username))
    }

    @Test
    fun `when a cached account has its failed-login lock released it is read unlocked`() {
        accountRepository.create(account.lockUntil(LOCKED_UNTIL))
        accountRepository.accountFor(account.username)

        uut.releaseIfLockedUntil(account.username, LOCKED_UNTIL)

        assertEquals(account.unlock(), accountRepository.accountFor(account.username))
    }
}
