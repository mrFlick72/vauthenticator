package com.vauthenticator.server.management

import com.vauthenticator.server.account.AccountRepositoryConfig
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.keys.domain.KeyRepository
import com.vauthenticator.server.keys.domain.KeyStorage
import com.vauthenticator.server.management.init.AccountSetUpJob
import com.vauthenticator.server.management.init.ClientApplicationSetUpJob
import com.vauthenticator.server.management.init.TenantSetUpEndPoint
import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.communication.domain.SmsType
import com.vauthenticator.server.document.adapter.FileSystemDocumentRepository
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import com.vauthenticator.server.oauth2.clientapp.ClientApplicationRepositoryConfig
import com.vauthenticator.server.page.PageConfig
import com.vauthenticator.server.page.domain.Page
import com.vauthenticator.server.page.domain.PageLocation
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.role.domain.RoleRepository
import com.vauthenticator.server.support.RepositoryConfigContextRunner.aRepositoryConfigContextRunner
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.context.SecurityContextHolder

class TenantInitConfigTest {

    @TempDir
    lateinit var documentBasePath: Path

    private val documentRepository by lazy { FileSystemDocumentRepository(documentBasePath.toString()) }

    private val contextRunner get() = aRepositoryConfigContextRunner(
        AccountRepositoryConfig::class.java,
        ClientApplicationRepositoryConfig::class.java,
        TenantInitConfig::class.java,
        PageConfig::class.java
    )
        .withPropertyValues("key.master-key.id=a-master-key")
        .withBean(RoleRepository::class.java, { mockk<RoleRepository>(relaxed = true) })
        .withBean(PasswordPolicy::class.java, { mockk<PasswordPolicy>(relaxed = true) })
        .withBean(VAuthenticatorPasswordEncoder::class.java, { mockk<VAuthenticatorPasswordEncoder>(relaxed = true) })
        .withBean(VAuthenticatorEventsDispatcher::class.java, { mockk<VAuthenticatorEventsDispatcher>(relaxed = true) })
        .withBean(KeyStorage::class.java, { mockk<KeyStorage>(relaxed = true) })
        .withBean(KeyRepository::class.java, { mockk<KeyRepository>(relaxed = true) })
        .withBean(DocumentRepository::class.java, { documentRepository })

    @Test
    fun `tenant setup creates the first administrator account and client app without a caller`() {
        SecurityContextHolder.clearContext()

        contextRunner.run { context ->
            assertThat(context).hasNotFailed()

            context.getBean(AccountSetUpJob::class.java).execute()
            context.getBean(ClientApplicationSetUpJob::class.java).execute()
        }
    }

    @Test
    fun `tenant setup gives a new tenant the default email and sms templates and the default pages`() {
        SecurityContextHolder.clearContext()

        contextRunner.run { context ->
            context.getBean(TenantSetUpEndPoint::class.java).tenantInit()

            EMailType.entries.forEach {
                assertThat(documentRepository.loadDocument(DocumentType.EMAIL.content, it.path).content).isNotEmpty()
            }
            SmsType.entries.forEach {
                assertThat(documentRepository.loadDocument(DocumentType.SMS.content, it.path).content).isNotEmpty()
            }
            val pageLocation = context.getBean(PageLocation::class.java)
            Page.entries.forEach {
                assertThat(
                    documentRepository.loadDocument(DocumentType.STATIC_ASSET.content, pageLocation.pathOf(it.bundle)).content
                ).isNotEmpty()
            }
        }
    }
}
