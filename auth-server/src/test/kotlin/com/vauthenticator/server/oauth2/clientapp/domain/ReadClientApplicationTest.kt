package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ReadClientApplicationTest {

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @Test
    fun `find one happy path`() {
        val clientAppId = ClientAppId("AN_ID")
        val readClientApplication = ReadClientApplication(clientApplicationRepository)

        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp(clientAppId)

        val actual: ClientApplication? = readClientApplication.findOne(clientAppId)
        assertEquals(actual, aClientApp(clientAppId = clientAppId, password = Secret("*******")))
    }

    @Test
    fun `find all happy path`() {
        val clientAppId = ClientAppId("AN_ID")
        val readClientApplication = ReadClientApplication(clientApplicationRepository)

        every { clientApplicationRepository.findAll() } returns listOf(aClientApp(clientAppId))

        val actual: List<ClientApplication> = readClientApplication.findAll()
        assertEquals(actual, listOf(aClientApp(clientAppId = clientAppId, password = Secret("*******"))))
    }
}