package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.support.AdministrationFixture.clientApplicationAdministrationFor
import com.vauthenticator.server.support.AdministrationFixture.notAnAdministrator
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StoreClientApplicationTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var passwordEncoder: VAuthenticatorPasswordEncoder

    lateinit var uut : StoreClientApplication

    @BeforeEach
    fun setUp() {
        uut = StoreClientApplication(
            clientApplicationRepository,
            clientApplicationAdministrationFor(notAnAdministrator),
            passwordEncoder
        )
    }

    @Test
    fun `store a client application for public client with a non empty password`() {
        val aClientApp = aClientApp(clientAppId = ClientAppId("AN_ID"), confidential = false, password = Secret("A_SECRET"))

        assertThrows(InvalidAppDataException::class.java) {
            uut.store(aClientApp, true)
        }

        verify(exactly = 0) { clientApplicationRepository.save(aClientApp) }
    }

    @Test
    fun `store a client application for public client with empty password`() {
        val aClientApp = aClientApp(clientAppId = ClientAppId("AN_ID"), confidential = false, password = Secret(""))

        every { clientApplicationRepository.findOne(aClientApp.clientAppId) } returns null
        every { passwordEncoder.encode(aClientApp.secret.content) } returns aClientApp.secret.content
        every { clientApplicationRepository.save(aClientApp) } just runs

        uut.store(aClientApp, true)
        verify { passwordEncoder.encode(aClientApp.secret.content) }
        verify { clientApplicationRepository.save(aClientApp) }
    }

    @Test
    fun `store a client application for confidential client with empty password`() {
        val aClientApp = aClientApp(clientAppId = ClientAppId("AN_ID"), confidential = true, password = Secret(""))

        assertThrows(InvalidAppDataException::class.java) {
            uut.store(aClientApp, true)
        }
    }

    @Test
    fun `store a client application with password`() {
        val aClientApp = aClientApp(ClientAppId("AN_ID"))

        every { clientApplicationRepository.findOne(aClientApp.clientAppId) } returns null
        every { passwordEncoder.encode(aClientApp.secret.content) } returns aClientApp.secret.content
        every { clientApplicationRepository.save(aClientApp) } just runs

        uut.store(aClientApp, true)
        verify { passwordEncoder.encode(aClientApp.secret.content) }
        verify { clientApplicationRepository.save(aClientApp) }
    }

    @Test
    fun `store a client application without password`() {
        val clientAppId = ClientAppId("AN_ID")
        val aClientApp = aClientApp(clientAppId)

        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp
        every { clientApplicationRepository.save(aClientApp) } just runs

        uut.store(aClientApp, false)

        verify(exactly = 0) { passwordEncoder.encode(aClientApp.secret.content) }
        verify { clientApplicationRepository.findOne(clientAppId) }
        verify { clientApplicationRepository.save(aClientApp) }
    }

    @Test
    fun `reset password for a public client application`() {
        val clientAppId = ClientAppId("AN_ID")
        val aClientApp = aClientApp(clientAppId=clientAppId, confidential = false, password =  Secret(""))

        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp

        assertThrows(UnsupportedClientAppOperationException::class.java) {
            uut.resetPassword(clientAppId, Secret("A_NEW_PASSWORD"))
        }
        verify { clientApplicationRepository.findOne(clientAppId) }
    }

    @Test
    fun `reset password for a confidential client application`() {
        val clientAppId = ClientAppId("AN_ID")
        val aClientApp = aClientApp(clientAppId)
        val updatedClientApp = aClientApp(clientAppId, password = Secret("A_NEW_PASSWORD"))

        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp
        every { passwordEncoder.encode("A_NEW_PASSWORD") } returns "A_NEW_PASSWORD"
        every { clientApplicationRepository.save(updatedClientApp) } just runs

        uut.resetPassword(clientAppId, Secret("A_NEW_PASSWORD"))

        verify { passwordEncoder.encode("A_NEW_PASSWORD") }
        verify { clientApplicationRepository.findOne(clientAppId) }
        verify { clientApplicationRepository.save(updatedClientApp) }
    }

    @Test
    fun `reset password for a not found client application`() {
        val clientAppId = ClientAppId("AN_ID")

        every { clientApplicationRepository.findOne(clientAppId) } returns null

        assertThrows(ClientApplicationNotFound::class.java) {
            uut.resetPassword(clientAppId, Secret("A_NEW_PASSWORD"))
        }
    }

    @Test
    fun `a caller that is not an administrator cannot store a client app with the full access scope`() {
        val anAdministratorClientApp = aClientApp(ClientAppId("AN_ID")).copy(scopes = Scopes.from(ADMIN_FULL_ACCESS))
        every { clientApplicationRepository.findOne(anAdministratorClientApp.clientAppId) } returns null
        every { passwordEncoder.encode(anAdministratorClientApp.secret.content) } returns "encoded"

        assertThrows(AdministrationRequiredException::class.java) {
            uut.store(anAdministratorClientApp, true)
        }

        verify(exactly = 0) { clientApplicationRepository.save(any()) }
    }

    @Test
    fun `a caller that is not an administrator cannot reset the secret of an administrator client app`() {
        val clientAppId = ClientAppId("admin")
        every { clientApplicationRepository.findOne(clientAppId) } returns
                aClientApp(clientAppId).copy(scopes = Scopes.from(ADMIN_FULL_ACCESS))
        every { passwordEncoder.encode("a secret of my choice") } returns "encoded"

        assertThrows(AdministrationRequiredException::class.java) {
            uut.resetPassword(clientAppId, Secret("a secret of my choice"))
        }

        verify(exactly = 0) { clientApplicationRepository.save(any()) }
    }
}
