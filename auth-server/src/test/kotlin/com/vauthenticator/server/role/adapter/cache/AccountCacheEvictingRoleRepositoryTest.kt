package com.vauthenticator.server.role.adapter.cache

import com.vauthenticator.server.account.adapter.CachedAccountRepository
import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.cache.RedisCacheOperation
import com.vauthenticator.server.role.adapter.jdbc.JdbcRoleRepository
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.RedisUtils.aRedisTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.time.Duration

class AccountCacheEvictingRoleRepositoryTest {

    private val accountCache = RedisCacheOperation<String, String>("account_cache_test", Duration.ofMinutes(1), aRedisTemplate())
    private val accountRepository = CachedAccountRepository(
        AccountCacheContentConverter(ObjectMapper()),
        accountCache,
        JdbcAccountRepository(jdbcTemplate)
    )
    private val uut = AccountCacheEvictingRoleRepository(JdbcRoleRepository(jdbcTemplate, emptyList()), accountCache)
    private val role = Role("A_ROLE", "A_ROLE")
    private val account = anAccount().copy(authorities = setOf(role.name))

    @BeforeEach
    fun setUp() {
        resetDb()
        accountCache.evictAll()
    }

    @Test
    fun `when a role is deleted a cached account that held it no longer has it`() {
        uut.save(role)
        accountRepository.create(account)
        accountRepository.accountFor(account.username)

        uut.delete(role.name)

        assertEquals(emptySet<String>(), accountRepository.accountFor(account.username)?.authorities)
    }
}
