package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.cache.CacheOperation
import com.vauthenticator.server.login.failedloginlock.adapter.cache.AccountCacheEvictingFailedLoginLockRepository
import com.vauthenticator.server.login.failedloginlock.adapter.jdbc.JdbcFailedLoginLockRepository
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration(proxyBeanMethods = false)
class FailedLoginLockRepositoryConfig {

    @Bean("failedLoginLockRepository")
    fun failedLoginLockRepository(
        jdbcTemplate: JdbcTemplate,
        @Qualifier("accountCacheOperation") accountCacheOperation: ObjectProvider<CacheOperation<String, String>>
    ): FailedLoginLockRepository {
        val jdbcFailedLoginLockRepository = JdbcFailedLoginLockRepository(jdbcTemplate)
        return accountCacheOperation.ifAvailable
            ?.let { AccountCacheEvictingFailedLoginLockRepository(jdbcFailedLoginLockRepository, it) }
            ?: jdbcFailedLoginLockRepository
    }
}
