package com.vauthenticator.server.oauth2.clientapp.ext

import com.vauthenticator.server.oauth2.clientapp.domain.InvalidAppDataException
import com.vauthenticator.server.oauth2.clientapp.domain.Secret
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient

class RegisteredClientExtTest {

    @Test
    fun `when a confidential client has a secret then it is returned`() {
        val registeredClient = registeredClient(confidential = true, clientSecret = "A_SECRET")

        val actual = registeredClient.clientSecret()

        assertEquals(Secret("A_SECRET"), actual)
    }

    @Test
    fun `when a client is public then an empty secret is returned`() {
        val registeredClient = registeredClient(confidential = false, clientSecret = null)

        val actual = registeredClient.clientSecret()

        assertEquals(Secret(""), actual)
    }

    @Test
    fun `when a confidential client has no secret then an invalid app data exception is thrown`() {
        val registeredClient = registeredClient(confidential = true, clientSecret = null)

        val actual = assertThrows(InvalidAppDataException::class.java) {
            registeredClient.clientSecret()
        }

        assertEquals(
            listOf("client_application.secret.empty", "client_application.secret.blank"),
            actual.validationResults["client_application.secret"]?.errorsCode
        )
        assertEquals(
            "Client app A_CLIENT_APP_ID secret is empty or blank and it is not supported for confidential client applications",
            actual.validationResults["client_application.secret"]?.errorMessage
        )
    }

    private fun registeredClient(confidential: Boolean, clientSecret: String?): RegisteredClient {
        val registeredClient = mockk<RegisteredClient>()

        every { registeredClient.clientAuthenticationMethods } returns if (confidential) {
            setOf(
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                ClientAuthenticationMethod.CLIENT_SECRET_POST
            )
        } else {
            setOf(ClientAuthenticationMethod.NONE)
        }
        every { registeredClient.clientId } returns "A_CLIENT_APP_ID"
        every { registeredClient.clientSecret } returns clientSecret

        return registeredClient
    }
}
