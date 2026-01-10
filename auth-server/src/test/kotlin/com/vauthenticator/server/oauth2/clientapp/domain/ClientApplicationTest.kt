package com.vauthenticator.server.oauth2.clientapp.domain

import com.vauthenticator.server.security.registeredclient.RegisteredClientRepositoryFixture.aClientApplication
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClientApplicationTest {

    @Test
    fun `when a client app is a configured to be public but has a client secret set`() {
        val aClientApplication = aClientApplication(confidential = false).get()
        assertThrows(InvalidAppDataException::class.java) { aClientApplication.validate() }
    }

    @Test
    fun `when a client app is a configured to be confidential but does not have a client secret set`() {
        val aClientApplication = aClientApplication().get().copy(secret = Secret(""))
        assertThrows(InvalidAppDataException::class.java) { aClientApplication.validate() }
    }

    @Test
    fun `when a client app has a client app id blank `() {
        val aClientApplication = aClientApplication().get().copy(clientAppId = ClientAppId(""))
        assertThrows(InvalidAppDataException::class.java) { aClientApplication.validate() }
    }

    @Test
    fun `when a client app supports authorization code flow is invalid`() {
        val aClientApplication = aClientApplication().get().copy(
            webServerRedirectUri = CallbackUri(""),
            postLogoutRedirectUri = PostLogoutRedirectUri(""),
            logoutUri = LogoutUri("")
        )
        assertThrows(InvalidAppDataException::class.java) { aClientApplication.validate() }
    }

    @Test
    fun `when a client app supports refresh token code flow is invalid`() {
        val aClientApplication = aClientApplication().get().copy(
            refreshTokenValidity = TokenTimeToLive(0)
        )
        assertThrows(InvalidAppDataException::class.java) { aClientApplication.validate() }
    }

    @Test
    fun `when a client app has access token ttl less than 0`() {
        val aClientApplication = aClientApplication().get().copy(
            accessTokenValidity = TokenTimeToLive(0)
        )
        assertThrows(InvalidAppDataException::class.java) { aClientApplication.validate() }
    }
}