package com.vauthenticator.server.mfa.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.mfa.domain.MfaMethod.EMAIL_MFA_METHOD
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.web.ExceptionAdviceController
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class MfaEnrolmentAssociationEndPointTest {

    lateinit var mokMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockK
    private lateinit var mfaAccountMethodsRepository: MfaAccountMethodsRepository

    @MockK
    private lateinit var mfaMethodsEnrollment: MfaMethodsEnrollment

    @MockK
    private lateinit var clientApplicationRepository: ClientApplicationRepository

    @MockK
    private lateinit var mfaMethodsEnrolmentAssociation: MfaMethodsEnrollmentAssociation


    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(
            MfaEnrolmentAssociationEndPoint(
                mfaAccountMethodsRepository,
                mfaMethodsEnrollment,
                mfaMethodsEnrolmentAssociation,
                PermissionValidator(clientApplicationRepository)
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }


    @Test
    fun `when all enrolment are retrieved`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        every {  mfaMethodsEnrollment.getEnrollmentsFor(email, true) } returns listOf(MfaDevice(email, EMAIL_MFA_METHOD, email, mfaDeviceId, true))

        mokMvc.perform(
            get("/api/mfa/enrollment")
                .principal(principalFor(email))
        )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    objectMapper.writeValueAsString(
                        listOf(
                            MfaDeviceRepresentation(email, EMAIL_MFA_METHOD, email, "AN_MFA_DEVICE_ID", true)
                        )
                    )
                )
            )
    }

    @Test
    fun `when a new mfa channel is enrolled`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        val authentication = principalFor(
            clientAppId = A_CLIENT_APP_ID,
            email = email,
            authorities = listOf("USER"),
            scopes = listOf(Scope.MFA_ENROLLMENT.content)
        )
        every {
            mfaMethodsEnrollment.enroll(
                account.email,
                EMAIL_MFA_METHOD,
                account.email,
                ClientAppId(A_CLIENT_APP_ID),
                true
            )
        } returns TicketId("A_TICKET")

        mokMvc.perform(
            post("/api/mfa/enrollment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentRequest(account.email, EMAIL_MFA_METHOD)))
                .principal(authentication)
        )
            .andExpect(status().isCreated)
            .andExpect(
                content().json(
                    objectMapper.writeValueAsString(
                        MfaEnrollmentResponse("A_TICKET")
                    )
                )
            )

    }

    @Test
    fun `when a new mfa channel is enrolled fails for insufficient scope`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        val authentication = principalFor(
            clientAppId = A_CLIENT_APP_ID,
            email = email,
            authorities = listOf("USER"),
            scopes = listOf(Scope.OPEN_ID.content)
        )
        every {
            mfaMethodsEnrollment.enroll(
                account.email,
                EMAIL_MFA_METHOD,
                account.email,
                ClientAppId(A_CLIENT_APP_ID),
                true
            )
        } returns TicketId("A_TICKET")

        mokMvc.perform(
            post("/api/mfa/enrollment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentRequest(account.email, EMAIL_MFA_METHOD)))
                .principal(authentication)
        ).andExpect(status().isForbidden)

    }


    @Test
    fun `when a new enrolled mfa is associated`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        val authentication = principalFor(
            clientAppId = A_CLIENT_APP_ID,
            email = email,
            authorities = listOf("USER"),
            scopes = listOf(Scope.MFA_ENROLLMENT.content)
        )
        every { mfaMethodsEnrolmentAssociation.associate("A_TICKET", "A_CODE") } just runs

        mokMvc.perform(
            post("/api/mfa/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentAssociationRequest("A_TICKET", "A_CODE")))
                .principal(authentication)
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `when a new enrolled mfa is associated fails for insufficient scope`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        val authentication = principalFor(
            clientAppId = A_CLIENT_APP_ID,
            email = email,
            authorities = listOf("USER"),
            scopes = listOf(Scope.OPEN_ID.content)
        )
        every { mfaMethodsEnrolmentAssociation.associate("A_TICKET", "A_CODE") } just runs

        mokMvc.perform(
            post("/api/mfa/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentAssociationRequest("A_TICKET", "A_CODE")))
                .principal(authentication)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `when an enrolled mfa is set as default`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        val authentication = principalFor(
            clientAppId = A_CLIENT_APP_ID,
            email = email,
            authorities = listOf("USER"),
            scopes = listOf(Scope.MFA_ENROLLMENT.content)
        )
        every { mfaAccountMethodsRepository.setAsDefault(email, MfaDeviceId("A_DEVICE_ID")) } just runs

        mokMvc.perform(
            put("/api/mfa/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SetDefaultMfaDeviceRequest("A_DEVICE_ID")))
                .principal(authentication)
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `when an enrolled mfa is set as default fails for insufficient scope`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email

        val authentication = principalFor(
            clientAppId = A_CLIENT_APP_ID,
            email = email,
            authorities = listOf("USER"),
            scopes = listOf(Scope.OPEN_ID.content)
        )
        every { mfaAccountMethodsRepository.setAsDefault(email, MfaDeviceId("A_DEVICE_ID")) } just runs

        mokMvc.perform(
            put("/api/mfa/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SetDefaultMfaDeviceRequest("A_DEVICE_ID")))
                .principal(authentication)
        ).andExpect(status().isForbidden)
    }
}