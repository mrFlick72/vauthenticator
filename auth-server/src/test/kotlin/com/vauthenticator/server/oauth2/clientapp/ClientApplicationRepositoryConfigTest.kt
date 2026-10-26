package com.vauthenticator.server.oauth2.clientapp

import com.vauthenticator.server.oauth2.clientapp.adapter.cache.CachedClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.support.RepositoryConfigContextRunner.aRepositoryConfigContextRunner
import com.vauthenticator.server.support.RepositoryConfigContextRunner.cacheEnabledFor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientApplicationRepositoryConfigTest {

    private val contextRunner = aRepositoryConfigContextRunner(ClientApplicationRepositoryConfig::class.java)

    @Test
    fun `when the client application cache is enabled client applications are read through the cache`() {
        contextRunner
            .withPropertyValues(*cacheEnabledFor("client-application"))
            .run { context ->
                assertThat(context.getBean(ClientApplicationRepository::class.java))
                    .isInstanceOf(CachedClientApplicationRepository::class.java)
            }
    }
}
