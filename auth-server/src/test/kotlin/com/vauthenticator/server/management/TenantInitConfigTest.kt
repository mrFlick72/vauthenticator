package com.vauthenticator.server.management

import com.vauthenticator.server.account.AccountRepositoryConfig
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.management.init.AccountSetUpJob
import com.vauthenticator.server.management.init.ClientApplicationSetUpJob
import com.vauthenticator.server.oauth2.clientapp.ClientApplicationRepositoryConfig
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.RepositoryConfigContextRunner.aRepositoryConfigContextRunner
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder

class TenantInitConfigTest {

    private val contextRunner = aRepositoryConfigContextRunner(
        AccountRepositoryConfig::class.java,
        ClientApplicationRepositoryConfig::class.java,
        TenantInitConfig::class.java
    )
        .withPropertyValues("key.master-key.id=a-master-key")
        .withBean(RoleRepository::class.java, { mockk<RoleRepository>(relaxed = true) })
        .withBean(PasswordPolicy::class.java, { mockk<PasswordPolicy>(relaxed = true) })
        .withBean(VAuthenticatorPasswordEncoder::class.java, { mockk<VAuthenticatorPasswordEncoder>(relaxed = true) })
        .withBean(VAuthenticatorEventsDispatcher::class.java, { mockk<VAuthenticatorEventsDispatcher>(relaxed = true) })
        .withBean(KeyStorage::class.java, { mockk<KeyStorage>(relaxed = true) })
        .withBean(KeyRepository::class.java, { mockk<KeyRepository>(relaxed = true) })

    @Test
    fun `tenant setup creates the first administrator account and client app without a caller`() {
        SecurityContextHolder.clearContext()

        contextRunner.run { context ->
            assertThat(context).hasNotFailed()

            context.getBean(AccountSetUpJob::class.java).execute()
            context.getBean(ClientApplicationSetUpJob::class.java).execute()
        }
    }
}
