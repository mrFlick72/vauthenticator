package com.vauthenticator.server.account.adapter

import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.cache.RedisCacheOperation
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

class CachedJdbcAccountRepositoryTest {

    private val accountCache = RedisCacheOperation<String, String>("account_cache_test", Duration.ofMinutes(1), aRedisTemplate())
    private val uut = CachedAccountRepository(
        AccountCacheContentConverter(ObjectMapper()),
        accountCache,
        JdbcAccountRepository(jdbcTemplate)
    )
    private val account = anAccount().copy(accountNonExpired = true, credentialsNonExpired = true)

    @BeforeEach
    fun setUp() {
        resetDb()
        accountCache.evictAll()
    }

    @Test
    fun `when a locked account is read from the cache it is still locked`() {
        uut.create(account.lock())
        uut.accountFor(account.username)

        assertEquals(account.lock(), uut.accountFor(account.username))
    }

    @Test
    fun `when an account under a failed-login lock is read from the cache it keeps the lock end`() {
        uut.create(account.lockUntil(LOCKED_UNTIL))
        uut.accountFor(account.username)

        assertEquals(account.lockUntil(LOCKED_UNTIL), uut.accountFor(account.username))
    }
}
