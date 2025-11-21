package com.vauthenticator.server.oauth2.clientapp.adapter

import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigin
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOriginRepository
import com.vauthenticator.server.oauth2.clientapp.domain.AllowedOrigins
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

abstract class AbstractAllowedOriginRepositoryTest {


    lateinit var uut: AllowedOriginRepository

    @BeforeEach
    fun setUp() {
        uut = initUnitUnderTest()
        resetDatabase()
    }

    abstract fun resetDatabase()
    abstract fun initUnitUnderTest(): AllowedOriginRepository

    @Test
    fun `when a application startup happen`() {
        val expected = setOf(AllowedOrigin("http://localhost:8080"), AllowedOrigin("http://localhost:9090"))
        val actual = uut.getAllAvailableAllowedOrigins()

        assertEquals(expected, actual)
    }

    @Test
    fun `when a new client application has been added`() {
        uut.setAllowedOriginsFor(
            ClientAppId("ANOTHER_AGAIN_CLIENT_APP_ID"),
            AllowedOrigins(setOf(AllowedOrigin("http://localhost:6060")))
        )
        val expected = setOf(
            AllowedOrigin("http://localhost:8080"),
            AllowedOrigin("http://localhost:9090"),
            AllowedOrigin("http://localhost:6060")
        )
        val actual = uut.getAllAvailableAllowedOrigins()

        assertEquals(expected, actual)
    }

    @Test
    fun `when a client application has been updated`() {
        uut.setAllowedOriginsFor(
            ClientAppId("ANOTHER_CLIENT_APP_ID"),
            AllowedOrigins(setOf(AllowedOrigin("http://localhost:6060")))
        )
        val expected = setOf(AllowedOrigin("http://localhost:8080"), AllowedOrigin("http://localhost:6060"))
        val actual = uut.getAllAvailableAllowedOrigins()

        assertEquals(expected, actual)
    }

    @Test
    fun `when a client application has been deleted`() {
        uut.deleteAllowedOriginsFor(ClientAppId("ANOTHER_CLIENT_APP_ID"))
        val expected = setOf(AllowedOrigin("http://localhost:8080"))
        val actual = uut.getAllAvailableAllowedOrigins()

        assertEquals(expected, actual)
    }
}