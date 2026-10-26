package com.vauthenticator.server.role

import com.vauthenticator.server.role.adapter.CachedRoleRepository
import com.vauthenticator.server.role.adapter.cache.AccountCacheEvictingGroupRepository
import com.vauthenticator.server.role.adapter.cache.AccountCacheEvictingRoleRepository
import com.vauthenticator.server.role.domain.GroupRepository
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.RepositoryConfigContextRunner.aRepositoryConfigContextRunner
import com.vauthenticator.server.support.RepositoryConfigContextRunner.cacheEnabledFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoleRepositoryConfigTest {

    private val contextRunner = aRepositoryConfigContextRunner(RoleRepositoryConfig::class.java)
        .withPropertyValues("vauthenticator.role.protected-from-delete=ROLE_USER")

    @Test
    fun `when the role cache is enabled roles are read through the cache`() {
        contextRunner
            .withPropertyValues(*cacheEnabledFor("role"))
            .run { context ->
                assertThat(context.getBean(RoleRepository::class.java)).isInstanceOf(CachedRoleRepository::class.java)
            }
    }

    @Test
    fun `when the account cache is enabled role deletes clear it`() {
        contextRunner
            .withPropertyValues(*cacheEnabledFor("account"))
            .run { context ->
                assertThat(context.getBean(RoleRepository::class.java)).isInstanceOf(AccountCacheEvictingRoleRepository::class.java)
            }
    }

    @Test
    fun `when the account cache is enabled group deletes clear it`() {
        contextRunner
            .withPropertyValues(*cacheEnabledFor("account"))
            .run { context ->
                assertThat(context.getBean(GroupRepository::class.java)).isInstanceOf(AccountCacheEvictingGroupRepository::class.java)
            }
    }
}
