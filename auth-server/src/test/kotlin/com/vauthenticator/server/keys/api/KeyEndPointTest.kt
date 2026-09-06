package com.vauthenticator.server.keys.api

import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.KeysUtils.aKid
import com.vauthenticator.server.support.KeysUtils.aMasterKey
import com.vauthenticator.server.support.KeysUtils.anotherKid
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
import tools.jackson.databind.ObjectMapper
import java.security.KeyPairGenerator
import java.time.Duration

private const val API_PATH = "/api/keys"

@ExtendWith(MockKExtension::class)
class KeyEndPointTest {

    private lateinit var mokMvc: MockMvc

    @MockK
    lateinit var keyRepository: KeyRepository

    @MockK
    lateinit var signatureKeyRotation: SignatureKeyRotation

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val mapper = ObjectMapper()
    private val payload = mapOf("masterKey" to "A_MASTER_KEY", "kid" to "A_KID")
    private val deletePayload = mapOf("kid" to "A_KID", "key_purpose" to "SIGNATURE", "key_ttl" to 0L)
    private val rotationPayload = mapOf("kid" to "A_KID", "master_kid" to "A_MASTER_KEY", "key_ttl" to 100L)

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            KeyEndPoint(
                "A_MASTER_KEY",
                keyRepository,
                signatureKeyRotation,
                PermissionValidator(clientApplicationRepository)
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `when we are able to load master key, kid of all  keys`() {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.KEY_READER.content))

        every { keyRepository.signatureKeys() } returns Keys(
            listOf(
                Key(
                    DataKey.from("", ""),
                    aMasterKey,
                    aKid,
                    true,
                    KeyType.ASYMMETRIC,
                    KeyPurpose.SIGNATURE,
                    0L
                )
            )
        )

        println(mapper.writeValueAsString(listOf(payload)))

        mokMvc.perform(get(API_PATH).principal(jwtAuthenticationToken))
            .andExpect(status().isOk)
            .andExpect(content().json(mapper.writeValueAsString(listOf(payload))))
    }

    @Test
    fun `loading all keys fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(get(API_PATH).principal(jwtAuthenticationToken))
            .andExpect(status().isForbidden)

        verify(exactly = 0) { keyRepository.signatureKeys() }
    }

    @Test
    fun `loading all keys with admin full access scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.ADMIN_FULL_ACCESS.content))

        every { keyRepository.signatureKeys() } returns Keys(
            listOf(
                Key(
                    DataKey.from("", ""),
                    aMasterKey,
                    aKid,
                    true,
                    KeyType.ASYMMETRIC,
                    KeyPurpose.SIGNATURE,
                    0L
                )
            )
        )

        mokMvc.perform(get(API_PATH).principal(jwtAuthenticationToken))
            .andExpect(status().isOk)
    }

    @Test
    fun `when we are able to create a new key`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.KEY_EDITOR.content))

        every { keyRepository.createKeyFrom(aMasterKey) } returns Kid("123")

        mokMvc.perform(post(API_PATH).principal(jwtAuthenticationToken))
            .andExpect(status().isCreated)

    }

    @Test
    fun `creating a new key fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(post(API_PATH).principal(jwtAuthenticationToken))
            .andExpect(status().isForbidden)

        verify(exactly = 0) { keyRepository.createKeyFrom(aMasterKey) }
    }

    @Test
    fun `creating a new key with admin full access scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.ADMIN_FULL_ACCESS.content))

        every { keyRepository.createKeyFrom(aMasterKey) } returns Kid("123")

        mokMvc.perform(post(API_PATH).principal(jwtAuthenticationToken))
            .andExpect(status().isCreated)
    }

    @Test
    fun `when we are able to delete a new key`() {
        every { keyRepository.deleteKeyFor(aKid, KeyPurpose.SIGNATURE) } just runs

        mokMvc.perform(
            delete(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(deletePayload))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `when we are not able to delete a new key`() {
        every { keyRepository.deleteKeyFor(aKid, KeyPurpose.SIGNATURE) } throws KeyDeletionException("")

        mokMvc.perform(
            delete(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(deletePayload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when we rotate a signature key`() {
        val expectedKid = anotherKid

        every {
            signatureKeyRotation.rotate(
                aMasterKey,
                aKid,
                Duration.ofSeconds(100)
            )
        } returns expectedKid

        mokMvc.perform(
            post("/api/keys/rotate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(rotationPayload))
        )
            .andExpect(status().isNoContent)

        verify {
            signatureKeyRotation.rotate(
                aMasterKey,
                aKid,
                Duration.ofSeconds(100)
            )
        }
    }
}