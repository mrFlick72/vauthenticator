package com.vauthenticator.server.keys.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.keys.domain.*
import com.vauthenticator.server.support.KeysUtils
import com.vauthenticator.server.support.KeysUtils.aKid
import com.vauthenticator.server.support.KeysUtils.aMasterKey
import com.vauthenticator.server.support.KeysUtils.anotherKid
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
import java.security.KeyPairGenerator
import java.time.Duration

private const val API_PATH = "/api/keys"

@ExtendWith(MockKExtension::class)
internal class KeyEndPointTest {

    private lateinit var mokMvc: MockMvc

    @MockK
    lateinit var keyRepository: KeyRepository

    @MockK
    lateinit var signatureKeyRotation: SignatureKeyRotation

    private val mapper = ObjectMapper()
    private val payload = mapOf("masterKey" to "A_MASTER_KEY", "kid" to "A_KID")
    private val deletePayload = mapOf("kid" to "A_KID", "key_purpose" to "SIGNATURE", "key_ttl" to 0L)
    private val rotationPayload = mapOf("kid" to "A_KID", "master_kid" to "A_MASTER_KEY", "key_ttl" to 100L)

    @BeforeEach
    internal fun setUp() {
        mokMvc = standaloneSetup(KeyEndPoint("A_MASTER_KEY", keyRepository, signatureKeyRotation)).build()
    }

    @Test
    internal fun `when we are able to load master key, kid of all  keys`() {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(2048)

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

        mokMvc.perform(get(API_PATH))
            .andExpect(status().isOk)
            .andExpect(content().json(mapper.writeValueAsString(listOf(payload))))
    }

    @Test
    internal fun `when we are able to create a new key`() {
        every { keyRepository.createKeyFrom(aMasterKey) } returns Kid("123")

        mokMvc.perform(post(API_PATH))
            .andExpect(status().isCreated)

    }

    @Test
    internal fun `when we are able to delete a new key`() {
        every { keyRepository.deleteKeyFor(aKid, KeyPurpose.SIGNATURE) } just runs

        mokMvc.perform(
            delete(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(deletePayload))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    internal fun `when we are not able to delete a new key`() {
        every { keyRepository.deleteKeyFor(aKid, KeyPurpose.SIGNATURE) } throws KeyDeletionException("")

        mokMvc.perform(
            delete(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(deletePayload))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    internal fun `when we rotate a signature key`() {
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