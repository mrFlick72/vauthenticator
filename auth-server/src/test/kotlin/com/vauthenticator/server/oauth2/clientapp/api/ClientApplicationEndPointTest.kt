package com.vauthenticator.server.oauth2.clientapp.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.oauth2.clientapp.domain.*
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.web.ExceptionAdviceController
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import java.util.*

@ExtendWith(MockKExtension::class)
class ClientApplicationEndPointTest {

    lateinit var mockMvc: MockMvc

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    lateinit var storeClientApplication: StoreClientApplication

    @MockK
    lateinit var readClientApplication: ReadClientApplication


    val objectMapper = ObjectMapper()

    @BeforeEach
    internal fun setUp() {
        mockMvc = standaloneSetup(
            ClientApplicationEndPoint(
                clientApplicationRepository,
                storeClientApplication,
                readClientApplication,
                PermissionValidator(clientApplicationRepository)
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }


    @Test
    fun `store a new client app fails for insufficient scope`() {
        val clientAppId = aClientAppId()
        val clientApplication = aClientApp(clientAppId)
        val representation = ClientAppRepresentation.fromDomainToRepresentation(clientApplication, storePassword = true)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mockMvc.perform(
            put("/api/client-applications/${clientAppId.content}").content(
                objectMapper.writeValueAsString(
                    representation
                )
            ).principal(jwtAuthenticationToken)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { storeClientApplication.store(clientApplication, true) }
    }

    @Test
    fun `store a new client app`() {
        val clientAppId = aClientAppId()
        val clientApplication = aClientApp(clientAppId)
        val representation = ClientAppRepresentation.fromDomainToRepresentation(clientApplication, storePassword = true)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_CLIENT_APPLICATION.content))

        every { storeClientApplication.store(clientApplication, true) } just runs

        mockMvc.perform(
            put("/api/client-applications/${clientAppId.content}").content(
                objectMapper.writeValueAsString(
                    representation
                )
            ).contentType(MediaType.APPLICATION_JSON)
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isNoContent)

    }
    @Test
    fun `store a new client app fails for unsupported action`() {
        val clientAppId = aClientAppId()
        val clientApplication = aClientApp(clientAppId)
        val representation = ClientAppRepresentation.fromDomainToRepresentation(clientApplication, storePassword = true)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_CLIENT_APPLICATION.content))

        every { storeClientApplication.store(clientApplication, true) } throws UnsupportedClientAppOperationException("irrelevant")

        mockMvc.perform(
            put("/api/client-applications/${clientAppId.content}").content(
                objectMapper.writeValueAsString(
                    representation
                )
            ).contentType(MediaType.APPLICATION_JSON)
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isInternalServerError)

    }

    @Test
    fun `reset password for a client app`() {
        val clientAppId = aClientAppId()
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_CLIENT_APPLICATION.content))

        every { storeClientApplication.resetPassword(clientAppId, Secret("secret")) } just runs

        mockMvc.perform(
            patch("/api/client-applications/${clientAppId.content}/client-secret").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientAppSecretRepresentation("secret")))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isNoContent)

    }

    @Test
    fun `reset password for a client app  fails for unsupported action`() {
        val clientAppId = aClientAppId()
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_CLIENT_APPLICATION.content))

        every { storeClientApplication.resetPassword(clientAppId, Secret("secret")) } throws UnsupportedClientAppOperationException("irrelevant")

        mockMvc.perform(
            patch("/api/client-applications/${clientAppId.content}/client-secret").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientAppSecretRepresentation("secret")))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isInternalServerError)

    }

    @Test
    fun `reset password for a client app fails for insufficient scope`() {
        val clientAppId = aClientAppId()
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        every { storeClientApplication.resetPassword(clientAppId, Secret("secret")) } just runs

        mockMvc.perform(
            patch("/api/client-applications/${clientAppId.content}/client-secret").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientAppSecretRepresentation("secret")))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

    }

    @Test
    fun `reset password for a not existing client app`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_CLIENT_APPLICATION.content))

        every {
            storeClientApplication.resetPassword(
                ClientAppId("clientApp"), Secret("secret")
            )
        } throws ClientApplicationNotFound("the client application clientApp was not found")

        mockMvc.perform(
            patch("/api/client-applications/clientApp/client-secret").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientAppSecretRepresentation("secret")))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `reset password for a not existing client app fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        every {
            storeClientApplication.resetPassword(
                ClientAppId("clientApp"), Secret("secret")
            )
        } throws ClientApplicationNotFound("the client application clientApp was not found")

        mockMvc.perform(
            patch("/api/client-applications/clientApp/client-secret").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientAppSecretRepresentation("secret")))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `view all client app`() {
        val clientApplication = aClientApp(ClientAppId("clientApp"))
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.READ_CLIENT_APPLICATION.content))

        val body = listOf(
            ClientAppInListRepresentation.fromDomainToRepresentation(clientApplication),
            ClientAppInListRepresentation.fromDomainToRepresentation(clientApplication),
            ClientAppInListRepresentation.fromDomainToRepresentation(clientApplication)
        )

        every { readClientApplication.findAll() } returns listOf(
            clientApplication, clientApplication, clientApplication
        )

        mockMvc.perform(
            get("/api/client-applications")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(body)))

    }

    @Test
    fun `view all client app fails for insufficient scope`() {
        val clientApplication = aClientApp(ClientAppId("clientApp"))
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        every { readClientApplication.findAll() } returns listOf(
            clientApplication, clientApplication, clientApplication
        )

        mockMvc.perform(
            get("/api/client-applications")
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `view a specific client app`() {
        val clientApplication = aClientApp(ClientAppId("clientApp"))
        val body = ClientAppRepresentation.fromDomainToRepresentation(clientApplication)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.READ_CLIENT_APPLICATION.content))

        every { readClientApplication.findOne(ClientAppId("clientApp")) } returns Optional.of(aClientApp(ClientAppId("clientApp")))

        mockMvc.perform(
            get("/api/client-applications/clientApp")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(body)))
    }

    @Test
    fun `when a client app does not exist`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.READ_CLIENT_APPLICATION.content))

        every { readClientApplication.findOne(ClientAppId("clientApp")) } returns Optional.empty()

        mockMvc.perform(
            get("/api/client-applications/clientApp")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `view a specific client app fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        every { readClientApplication.findOne(ClientAppId("clientApp")) } returns Optional.of(aClientApp(ClientAppId("clientApp")))

        mockMvc.perform(
            get("/api/client-applications/clientApp")
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `delete a specific client app`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.DELETE_CLIENT_APPLICATION.content))

        every { clientApplicationRepository.delete(ClientAppId("clientApp")) } just runs

        mockMvc.perform(
            delete("/api/client-applications/clientApp")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `delete a specific client app fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        every { clientApplicationRepository.delete(ClientAppId("clientApp")) } just runs

        mockMvc.perform(
            delete("/api/client-applications/clientApp")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isForbidden)
    }
}