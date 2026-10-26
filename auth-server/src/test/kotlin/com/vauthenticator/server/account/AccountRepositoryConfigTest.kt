package com.vauthenticator.server.account

import com.vauthenticator.server.account.adapter.CachedAccountRepository
import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.RepositoryConfigContextRunner.aRepositoryConfigContextRunner
import com.vauthenticator.server.support.RepositoryConfigContextRunner.cacheEnabledFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AccountRepositoryConfigTest {

    private val contextRunner = aRepositoryConfigContextRunner(AccountRepositoryConfig::class.java)

    @Test
    fun `when the account cache is not enabled accounts are read from the database`() {
        contextRunner.run { context ->
            assertThat(context.getBean(AccountRepository::class.java)).isInstanceOf(JdbcAccountRepository::class.java)
        }
    }

    @Test
    fun `when the account cache is enabled accounts are read through the cache`() {
        contextRunner
            .withPropertyValues(*cacheEnabledFor("account"))
            .run { context ->
                assertThat(context.getBean(AccountRepository::class.java)).isInstanceOf(CachedAccountRepository::class.java)
            }
    }
}
