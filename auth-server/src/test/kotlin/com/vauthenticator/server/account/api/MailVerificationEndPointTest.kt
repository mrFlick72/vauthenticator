package com.vauthenticator.server.account.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.emailverification.SendVerifyEMailChallenge
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture
import com.vauthenticator.server.support.SecurityFixture.signedJWTFor
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import java.time.Instant

@ExtendWith(MockKExtension::class)
class MailVerificationEndPointTest {
    private val objectMapper = ObjectMapper()

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var sendVerifyEMailChallenge: SendVerifyEMailChallenge

    @MockK
    lateinit var cientApplicationRepository: ClientApplicationRepository

    @BeforeEach
    fun setUp() {
        mokMvc = standaloneSetup(
            MailVerificationEndPoint(
                PermissionValidator(cientApplicationRepository),
                sendVerifyEMailChallenge
            )
        ).build()
    }

    @Test
    fun `when a challenge is sent`() {
        every { sendVerifyEMailChallenge.sendVerifyMail(EMAIL) } just runs

        val signedJWT = signedJWTFor(A_CLIENT_APP_ID, EMAIL, listOf(Scope.MAIL_VERIFY.content))
        val principal = JwtAuthenticationToken(
            Jwt(
                SecurityFixture.simpleJwtFor(A_CLIENT_APP_ID),
                Instant.now(),
                Instant.now().plusSeconds(100),
                signedJWT.header.toJSONObject(),
                signedJWT.payload.toJSONObject()
            )
        )

        mokMvc.perform(
            put("/api/verify-challenge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(mapOf("email" to EMAIL)))
                .principal(principal)
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `when a challenge api without request body`() {
        val signedJWT = signedJWTFor(A_CLIENT_APP_ID, EMAIL, listOf(Scope.MAIL_VERIFY.content))
        val principal = JwtAuthenticationToken(
            Jwt(
                SecurityFixture.simpleJwtFor(A_CLIENT_APP_ID),
                Instant.now(),
                Instant.now().plusSeconds(100),
                signedJWT.header.toJSONObject(),
                signedJWT.payload.toJSONObject()
            )
        )

        mokMvc.perform(
            put("/api/verify-challenge")
                .contentType(MediaType.APPLICATION_JSON)
                .principal(principal)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `when a challenge api is bad used`() {
        val signedJWT = signedJWTFor(A_CLIENT_APP_ID, EMAIL, listOf(Scope.MAIL_VERIFY.content))
        val principal = JwtAuthenticationToken(
            Jwt(
                SecurityFixture.simpleJwtFor(A_CLIENT_APP_ID),
                Instant.now(),
                Instant.now().plusSeconds(100),
                signedJWT.header.toJSONObject(),
                signedJWT.payload.toJSONObject()
            )
        )

        mokMvc.perform(
            put("/api/verify-challenge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(emptyMap<String, String>()))
                .principal(principal)
        )
            .andExpect(status().isBadRequest)
    }


}