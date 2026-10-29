package com.vauthenticator.server.mfa.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.mfa.domain.*
import com.vauthenticator.server.mfa.domain.MfaMethod.EMAIL_MFA_METHOD
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.oauth2.clientapp.domain.Scopes
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.ClientAppFixture.aClientApp
import com.vauthenticator.server.support.ClientAppFixture.aClientAppId
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.MfaFixture.mfaDeviceId
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
import com.vauthenticator.server.ticket.domain.TicketId
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class MfaEnrolmentAssociationEndPointTest {

    lateinit var mokMvc: MockMvc

    private val objectMapper = ObjectMapper()

    @MockK
    private lateinit var mfaDevices: MfaDevices

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
                mfaDevices,
                mfaMethodsEnrollment,
                mfaMethodsEnrolmentAssociation,
                PermissionValidator(clientApplicationRepository)
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }


    @Test
    fun `when all enrolment are retrieved by a session user`() {
        val account = AccountTestFixture.anAccount()
        val email = account.email
        val clientAppId = aClientAppId()

        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp(clientAppId).copy(scopes = Scopes.from(Scope.MFA_ENROLLMENT))
        every {  mfaMethodsEnrollment.getEnrollmentsFor(email, true) } returns listOf(MfaDevice(email, EMAIL_MFA_METHOD, email, mfaDeviceId, true))

        mokMvc.perform(
            get("/api/mfa/enrollment")
                .sessionAttr("clientId", clientAppId.content)
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
    fun `when all enrolment retrieval by a session user fails for insufficient client app scope`() {
        val clientAppId = aClientAppId()

        every { clientApplicationRepository.findOne(clientAppId) } returns aClientApp(clientAppId).copy(scopes = Scopes.from(Scope.MFA_ALWAYS))

        mokMvc.perform(
            get("/api/mfa/enrollment")
                .sessionAttr("clientId", clientAppId.content)
                .principal(principalFor(EMAIL))
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { mfaMethodsEnrollment.getEnrollmentsFor(any(), any()) }
    }

    @Test
    fun `when all enrolment are retrieved with a bearer JWT`() {
        val principal = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))

        every { mfaMethodsEnrollment.getEnrollmentsFor(EMAIL, true) } returns listOf(MfaDevice(EMAIL, EMAIL_MFA_METHOD, EMAIL, mfaDeviceId, true))

        mokMvc.perform(
            get("/api/mfa/enrollment")
                .principal(principal)
        ).andExpect(status().isOk)

        verify { mfaMethodsEnrollment.getEnrollmentsFor(EMAIL, true) }
    }

    @Test
    fun `when all enrolment retrieval with a bearer JWT fails for insufficient scope`() {
        val principal = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ALWAYS.content))

        mokMvc.perform(
            get("/api/mfa/enrollment")
                .principal(principal)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { mfaMethodsEnrollment.getEnrollmentsFor(any(), any()) }
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
        every { mfaMethodsEnrolmentAssociation.associate(email, "A_TICKET", "A_CODE") } just runs

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
        every { mfaMethodsEnrolmentAssociation.associate(email, "A_TICKET", "A_CODE") } just runs

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
        every { mfaDevices.setAsDefault(email, MfaDeviceId("A_DEVICE_ID")) } just runs

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
        every { mfaDevices.setAsDefault(email, MfaDeviceId("A_DEVICE_ID")) } just runs

        mokMvc.perform(
            put("/api/mfa/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SetDefaultMfaDeviceRequest("A_DEVICE_ID")))
                .principal(authentication)
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `when a new mfa channel is enrolled is unauthorized for a session user`() {
        val sessionUser = principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN))

        mokMvc.perform(
            post("/api/mfa/enrollment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentRequest(EMAIL, EMAIL_MFA_METHOD)))
                .principal(sessionUser)
        ).andExpect(status().isUnauthorized)

        verify(exactly = 0) { mfaMethodsEnrollment.enroll(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `when a new enrolled mfa is associated is unauthorized for a session user`() {
        val sessionUser = principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN))

        mokMvc.perform(
            post("/api/mfa/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentAssociationRequest("A_TICKET", "A_CODE")))
                .principal(sessionUser)
        ).andExpect(status().isUnauthorized)

        verify(exactly = 0) { mfaMethodsEnrolmentAssociation.associate(any<String>(), any<String>(), any<String>(), any()) }
    }

    @Test
    fun `when an enrolled mfa is set as default is unauthorized for a session user`() {
        val sessionUser = principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN))

        mokMvc.perform(
            put("/api/mfa/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SetDefaultMfaDeviceRequest("A_DEVICE_ID")))
                .principal(sessionUser)
        ).andExpect(status().isUnauthorized)

        verify(exactly = 0) { mfaDevices.setAsDefault(any(), any()) }
    }

    @Test
    fun `when an enrolled mfa set as default is not an associated device of the account`() {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))
        every { mfaDevices.setAsDefault(EMAIL, MfaDeviceId("A_DEVICE_ID")) } throws MfaDeviceNotFoundException("not found")

        mokMvc.perform(
            put("/api/mfa/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(SetDefaultMfaDeviceRequest("A_DEVICE_ID")))
                .principal(authentication)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `when a mfa device is revoked`() {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))
        every { mfaDevices.revoke(EMAIL, MfaDeviceId("A_DEVICE_ID"), ClientAppId(A_CLIENT_APP_ID)) } just runs

        mokMvc.perform(
            delete("/api/mfa/enrollment/A_DEVICE_ID")
                .principal(authentication)
        ).andExpect(status().isNoContent)

        verify { mfaDevices.revoke(EMAIL, MfaDeviceId("A_DEVICE_ID"), ClientAppId(A_CLIENT_APP_ID)) }
    }

    @Test
    fun `when a mfa device to revoke is not found`() {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))
        every { mfaDevices.revoke(EMAIL, MfaDeviceId("A_DEVICE_ID"), ClientAppId(A_CLIENT_APP_ID)) } throws
                MfaDeviceNotFoundException("not found")

        mokMvc.perform(
            delete("/api/mfa/enrollment/A_DEVICE_ID")
                .principal(authentication)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `when a mfa device revocation is refused`() {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))
        every { mfaDevices.revoke(EMAIL, MfaDeviceId("A_DEVICE_ID"), ClientAppId(A_CLIENT_APP_ID)) } throws
                MfaDeviceRevocationException(MfaDeviceRevocationRefusal.DEFAULT_DEVICE)

        mokMvc.perform(
            delete("/api/mfa/enrollment/A_DEVICE_ID")
                .principal(authentication)
        )
            .andExpect(status().isConflict)
            .andExpect(content().json("""{"reason":"DEFAULT_DEVICE"}"""))
    }

    @Test
    fun `when a mfa device revocation fails for insufficient scope`() {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.OPEN_ID.content))

        mokMvc.perform(
            delete("/api/mfa/enrollment/A_DEVICE_ID")
                .principal(authentication)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { mfaDevices.revoke(any(), any(), any()) }
    }

    @Test
    fun `when a mfa device revocation is unauthorized for a session user`() {
        val sessionUser = principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN))

        mokMvc.perform(
            delete("/api/mfa/enrollment/A_DEVICE_ID")
                .principal(sessionUser)
        ).andExpect(status().isUnauthorized)

        verify(exactly = 0) { mfaDevices.revoke(any(), any(), any()) }
    }

    @Test
    fun `when a new mfa channel to enroll is already associated`() {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))
        every { mfaMethodsEnrollment.enroll(EMAIL, EMAIL_MFA_METHOD, EMAIL, ClientAppId(A_CLIENT_APP_ID), true) } throws
                MfaAssociationFailureException(MfaAssociationFailureReason.ALREADY_ASSOCIATED, "already associated")

        mokMvc.perform(
            post("/api/mfa/enrollment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentRequest(EMAIL, EMAIL_MFA_METHOD)))
                .principal(authentication)
        )
            .andExpect(status().isConflict)
            .andExpect(content().json("""{"reason":"ALREADY_ASSOCIATED"}"""))
    }

    @Test
    fun `when a new enrolled mfa association fails for a wrong code`() {
        assertAssociationFailure(MfaAssociationFailureReason.WRONG_CODE, 422)
    }

    @Test
    fun `when a new enrolled mfa association fails for an invalid ticket`() {
        assertAssociationFailure(MfaAssociationFailureReason.INVALID_TICKET, 410)
    }

    @Test
    fun `when a new enrolled mfa association fails for an already associated device`() {
        assertAssociationFailure(MfaAssociationFailureReason.ALREADY_ASSOCIATED, 409)
    }

    private fun assertAssociationFailure(reason: MfaAssociationFailureReason, expectedStatus: Int) {
        val authentication = principalFor(A_CLIENT_APP_ID, EMAIL, scopes = listOf(Scope.MFA_ENROLLMENT.content))
        every { mfaMethodsEnrolmentAssociation.associate(EMAIL, "A_TICKET", "A_CODE") } throws
                MfaAssociationFailureException(reason, "association failed")

        mokMvc.perform(
            post("/api/mfa/associate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MfaEnrollmentAssociationRequest("A_TICKET", "A_CODE")))
                .principal(authentication)
        )
            .andExpect(status().`is`(expectedStatus))
            .andExpect(content().json("""{"reason":"${reason.name}"}"""))
    }
}
