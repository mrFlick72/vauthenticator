package com.vauthenticator.server.oauth2.clientapp.adapter

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigins.Companion.empty
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigins.Companion.from
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplication
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
abstract class AbstractClientApplicationRepositoryTest {

    private lateinit var uut: ClientApplicationRepository

    abstract fun resetDatabase()
    abstract fun initUnitUnderTest(allowedOriginRepository1: AllowedOriginRepository): ClientApplicationRepository

    @MockK
    lateinit var allowedOriginRepository: AllowedOriginRepository

    @BeforeEach
    fun setUp() {
        every { allowedOriginRepository.setAllowedOriginsFor(ClientAppId("client_id"), from(AllowedOrigin("*"))) } just runs
        uut = initUnitUnderTest(allowedOriginRepository)
        resetDatabase()
    }

    @Test
    fun `when find one client application by empty client id`() {
        val clientApp: Optional<ClientApplication> = uut.findOne(ClientAppId(""))
        val expected = Optional.empty<ClientApplication>()
        assertEquals(expected, clientApp)
    }

    @Test
    fun `when find one client application by client id that does not exist`() {
        val clientApp: Optional<ClientApplication> =
            uut.findOne(ClientAppId("not-existing-one"))
        val expected = Optional.empty<ClientApplication>()
        assertEquals(expected, clientApp)
    }

    @Test
    fun `when store, check if it exist and then delete a client application by client`() {
        val clientAppId = ClientAppId("client_id")
        val expected = aClientApp(clientAppId)
        saveClientAppWith(expected)
        var actual = uut.findOne(clientAppId)

        assertEquals(Optional.of(expected), actual)

        deleteClientApplication(clientAppId)
        actual = uut.findOne(clientAppId)

        assertEquals(Optional.empty<ClientApplication>(), actual)
    }

    @Test
    fun `when store a public client application, check if it exist and then delete a client application by client`() {
        val clientAppId = ClientAppId("client_id")
        val expected = aClientApp(clientAppId).copy(confidential = false)
        saveClientAppWith(expected)
        var actual = uut.findOne(clientAppId)

        assertEquals(Optional.of(expected), actual)

        deleteClientApplication(clientAppId)
        actual = uut.findOne(clientAppId)

        assertEquals(Optional.empty<ClientApplication>(), actual)
    }

    @Test
    fun `when find all client applications`() {
        val clientAppId = ClientAppId("client_id")
        val expected = aClientApp(clientAppId)
        saveClientAppWith(expected)
        val actual = uut.findAll()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun `when find an client application with zero authorities`() {
        val clientAppId = ClientAppId("client_id")
        val expected = aClientApp(clientAppId)
        saveClientAppWith(expected)
        val actual = uut.findAll()

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun `when store a client application with zero allowedOrigins`() {
        val clientAppId = ClientAppId("client_id")
        val clientApplication = aClientApp(clientAppId).copy(confidential = false, allowedOrigins = empty())
        val expected = clientApplication.copy(allowedOrigins = from(AllowedOrigin("*")))

        saveClientAppWith(clientApplication)
        var actual = uut.findOne(clientAppId)

        assertEquals(Optional.of(expected), actual)
    }

    private fun saveClientAppWith(expected: ClientApplication) {
        every { allowedOriginRepository.setAllowedOriginsFor(expected.clientAppId, expected.allowedOrigins) } just runs
        uut.save(expected)
        verify { allowedOriginRepository.setAllowedOriginsFor(expected.clientAppId, expected.allowedOrigins) }
    }

    private fun deleteClientApplication(clientAppId: ClientAppId) {
        every { allowedOriginRepository.deleteAllowedOriginsFor(clientAppId) } just runs

        uut.delete(clientAppId)

        verify { allowedOriginRepository.deleteAllowedOriginsFor(clientAppId) }
    }
}