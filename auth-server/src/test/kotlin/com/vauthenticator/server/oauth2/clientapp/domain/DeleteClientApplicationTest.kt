package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.oauth2.clientapp.domain.Scope.Companion.ADMIN_FULL_ACCESS
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.support.AdministrationFixture.clientApplicationAdministrationFor
import com.vauthenticator.server.support.AdministrationFixture.notAnAdministrator
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
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
class DeleteClientApplicationTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private lateinit var uut: DeleteClientApplication

    @BeforeEach
    fun setUp() {
        uut = DeleteClientApplication(clientApplicationRepository, clientApplicationAdministrationFor(notAnAdministrator))
        every { clientApplicationRepository.delete(any()) } just runs
    }

    @Test
    fun `a caller that is not an administrator deletes a client app that is not an administrator`() {
        every { clientApplicationRepository.findOne(aClientAppId()) } returns aClientApp(aClientAppId())

        uut.delete(aClientAppId())

        verify { clientApplicationRepository.delete(aClientAppId()) }
    }

    @Test
    fun `a caller that is not an administrator cannot delete an administrator client app`() {
        val clientAppId = ClientAppId("admin")
        every { clientApplicationRepository.findOne(clientAppId) } returns
                aClientApp(clientAppId).copy(scopes = Scopes.from(ADMIN_FULL_ACCESS))

        assertThrows(AdministrationRequiredException::class.java) { uut.delete(clientAppId) }

        verify(exactly = 0) { clientApplicationRepository.delete(any()) }
    }
}
