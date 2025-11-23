package com.vauthenticator.server.security.registeredclient

import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.oauth2.registeredclient.ClientAppRegisteredClientRepository
import com.vauthenticator.server.oauth2.registeredclient.RegisteredClientAppNotFound
import com.vauthenticator.server.security.registeredclient.RegisteredClientRepositoryFixture.aClientApplication
import com.vauthenticator.server.security.registeredclient.RegisteredClientRepositoryFixture.aRegisteredClient
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient.*
import java.util.*

@ExtendWith(MockKExtension::class)
class ClientAppRegisteredClientRepositoryTest {

    @MockK
    lateinit var storeClientApplication: StoreClientApplication

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private lateinit var uut: ClientAppRegisteredClientRepository

    @BeforeEach
    fun setup() {
        uut = ClientAppRegisteredClientRepository(storeClientApplication, clientApplicationRepository)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true , false])
    fun `when find a client app by id`(confidential : Boolean) {
        every { clientApplicationRepository.findOne(ClientAppId("A_CLIENT_APP_ID")) }
            .returns(aClientApplication(confidential))

        val actual = uut.findById("A_CLIENT_APP_ID")

        assertEquals(aRegisteredClient(confidential), actual)
    }


    @ParameterizedTest
    @ValueSource(booleans = [true , false])
    fun `when find a client app by client id`(confidential : Boolean) {
        every { clientApplicationRepository.findOne(ClientAppId("A_CLIENT_APP_ID")) }
            .returns(aClientApplication(confidential))

        val actual = uut.findByClientId("A_CLIENT_APP_ID")

        assertEquals(aRegisteredClient(confidential), actual)
    }


    @Test
    fun `when application is not found by id or clientId`() {
        every { clientApplicationRepository.findOne(ClientAppId("A_CLIENT_APP_ID")) }
            .returns(Optional.empty())

        assertThrows(
            RegisteredClientAppNotFound::class.java,
            { uut.findByClientId("A_CLIENT_APP_ID") },
            "Application with id or client_id: A_CLIENT_APP_ID not found"
        )

        assertThrows(
            RegisteredClientAppNotFound::class.java,
            { uut.findById("A_CLIENT_APP_ID") },
            "Application with id or client_id: A_CLIENT_APP_ID not found"
        )
    }


    @ParameterizedTest
    @ValueSource(booleans = [true , false])
    fun `when a new client app is registered`(confidential : Boolean) {
        every {
            storeClientApplication.store(
                aClientApplication().get()
                    .copy(
                        confidential = confidential,
                        allowedOrigins = AllowedOrigins.empty(),
                        postLogoutRedirectUri = PostLogoutRedirectUri("http://post_logout_redirect_uri"),
                        logoutUri = LogoutUri("http://post_logout_redirect_uri"),
                        scopes = Scopes(setOf(Scope("A_SCOPE"), Scope("ANOTHER_SCOPE")))
                    ), true
            )
        } just runs

        uut.save(from(aRegisteredClient(confidential)).build())

    }
}