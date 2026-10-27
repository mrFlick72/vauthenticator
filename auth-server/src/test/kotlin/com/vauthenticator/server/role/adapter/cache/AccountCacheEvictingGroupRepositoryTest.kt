package com.vauthenticator.server.role.adapter.cache

import com.vauthenticator.server.account.adapter.CachedAccountRepository
import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.cache.RedisCacheOperation
import com.vauthenticator.server.role.adapter.jdbc.JdbcGroupRepository
import com.vauthenticator.server.role.domain.Group
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.JdbcUtils.jdbcClient
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.RedisUtils.aRedisTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.Duration

class AccountCacheEvictingGroupRepositoryTest {

    private val accountCache = RedisCacheOperation<String, String>("account_cache_test", Duration.ofMinutes(1), aRedisTemplate())
    private val accountRepository = CachedAccountRepository(
        AccountCacheContentConverter(ObjectMapper()),
        accountCache,
        JdbcAccountRepository(jdbcTemplate)
    )
    private val uut = AccountCacheEvictingGroupRepository(JdbcGroupRepository(jdbcClient), accountCache)
    private val group = Group("A_GROUP", "A_GROUP")
    private val account = anAccount().copy(groups = setOf(group.name))

    @BeforeEach
    fun setUp() {
        resetDb()
        accountCache.evictAll()
    }

    @Test
    fun `when a group is deleted a cached account that was in it is no longer in it`() {
        uut.save(group)
        accountRepository.create(account)
        accountRepository.accountFor(account.username)

        uut.delete(group.name)

        assertEquals(emptySet<String>(), accountRepository.accountFor(account.username)?.groups)
    }
}
