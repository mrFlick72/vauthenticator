package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.login.failedloginlock.adapter.cache.AccountCacheEvictingFailedLoginLockRepository
import com.vauthenticator.server.support.RepositoryConfigContextRunner.aRepositoryConfigContextRunner
import com.vauthenticator.server.support.RepositoryConfigContextRunner.cacheEnabledFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FailedLoginLockRepositoryConfigTest {

    private val contextRunner = aRepositoryConfigContextRunner(FailedLoginLockRepositoryConfig::class.java)

    @Test
    fun `when the account cache is enabled failed-login lock updates evict it`() {
        contextRunner
            .withPropertyValues(*cacheEnabledFor("account"))
            .run { context ->
                assertThat(context.getBean(FailedLoginLockRepository::class.java))
                    .isInstanceOf(AccountCacheEvictingFailedLoginLockRepository::class.java)
            }
    }
}
