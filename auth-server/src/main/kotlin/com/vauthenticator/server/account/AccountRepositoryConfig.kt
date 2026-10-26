package com.vauthenticator.server.account

import com.vauthenticator.server.account.adapter.CachedAccountRepository
import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountCacheContentConverter
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.cache.CacheOperation
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper

@Configuration(proxyBeanMethods = false)
class AccountRepositoryConfig {

    @Bean("accountRepository")
    fun accountRepository(
        jdbcTemplate: JdbcTemplate,
        objectMapper: ObjectMapper,
        @Qualifier("accountCacheOperation") accountCacheOperation: ObjectProvider<CacheOperation<String, String>>
    ): AccountRepository {
        val jdbcAccountRepository = JdbcAccountRepository(jdbcTemplate)
        return accountCacheOperation.ifAvailable
            ?.let { CachedAccountRepository(AccountCacheContentConverter(objectMapper), it, jdbcAccountRepository) }
            ?: jdbcAccountRepository
    }
}
