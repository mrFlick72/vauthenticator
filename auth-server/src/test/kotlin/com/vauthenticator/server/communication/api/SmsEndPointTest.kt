package com.vauthenticator.server.communication.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.communication.domain.SmsTemplate
import com.vauthenticator.server.communication.domain.SmsType
import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.SecurityFixture.m2mPrincipalFor
import com.vauthenticator.server.support.SecurityFixture.principalFor
import com.vauthenticator.server.support.VAUTHENTICATOR_ADMIN
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class SmsEndPointTest {

    lateinit var mockMvc: MockMvc

    @MockK
    lateinit var documentRepository: DocumentRepository

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            SmsEndPoint(documentRepository, PermissionValidator(clientApplicationRepository))
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `when an mfa sms template is retrieved`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SMS_TEMPLATE_READER.content))
        val response = SmsTemplate(SmsType.MFA, "A_TEMPLATE")

        every {
            documentRepository.loadDocument(DocumentType.SMS.content, SmsType.MFA.path)
        } returns Document("", SmsType.MFA.path, "A_TEMPLATE".toByteArray())

        mockMvc.perform(
            get("/api/sms-template/${SmsType.MFA}")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(response)))
    }

    @Test
    fun `when a new sms template is uploaded`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SMS_TEMPLATE_WRITER.content))
        val request = SmsTemplate(SmsType.MFA, "your code is {{ mfaCode }}")

        every {
            documentRepository.saveDocument(
                DocumentType.SMS.content,
                Document("text/plain", "templates/mfa-challenge.txt", "your code is {{ mfaCode }}".toByteArray())
            )
        } just runs

        mockMvc.perform(
            put("/api/sms-template")
                .principal(jwtAuthenticationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNoContent)

        verify {
            documentRepository.saveDocument(
                DocumentType.SMS.content,
                Document("text/plain", "templates/mfa-challenge.txt", "your code is {{ mfaCode }}".toByteArray())
            )
        }
    }

    @Test
    fun `retrieving an sms template fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MAIL_TEMPLATE_READER.content))

        mockMvc.perform(
            get("/api/sms-template/${SmsType.MFA}")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { documentRepository.loadDocument(DocumentType.SMS.content, SmsType.MFA.path) }
    }

    @Test
    fun `retrieving an sms template with admin full access scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.ADMIN_FULL_ACCESS.content))

        every {
            documentRepository.loadDocument(DocumentType.SMS.content, SmsType.MFA.path)
        } returns Document("", SmsType.MFA.path, "A_TEMPLATE".toByteArray())

        mockMvc.perform(
            get("/api/sms-template/${SmsType.MFA}")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `uploading a new sms template fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MAIL_TEMPLATE_WRITER.content))
        val request = SmsTemplate(SmsType.MFA, "A_TEMPLATE")

        mockMvc.perform(
            put("/api/sms-template")
                .principal(jwtAuthenticationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { documentRepository.saveDocument(DocumentType.SMS.content, any()) }
    }

    @Test
    fun `uploading a new sms template with admin full access scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.ADMIN_FULL_ACCESS.content))
        val request = SmsTemplate(SmsType.MFA, "A_TEMPLATE")

        every { documentRepository.saveDocument(DocumentType.SMS.content, any()) } just runs

        mockMvc.perform(
            put("/api/sms-template")
                .principal(jwtAuthenticationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNoContent)
    }

    @Test
    fun `retrieving an sms template is unauthorized for a session user`() {
        val sessionUser = principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN))

        mockMvc.perform(
            get("/api/sms-template/${SmsType.MFA}")
                .principal(sessionUser)
        )
            .andExpect(status().isUnauthorized)

        verify(exactly = 0) { documentRepository.loadDocument(DocumentType.SMS.content, SmsType.MFA.path) }
    }

    @Test
    fun `uploading a new sms template is unauthorized for a session user`() {
        val sessionUser = principalFor(EMAIL, listOf(VAUTHENTICATOR_ADMIN))
        val request = SmsTemplate(SmsType.MFA, "A_TEMPLATE")

        mockMvc.perform(
            put("/api/sms-template")
                .principal(sessionUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isUnauthorized)

        verify(exactly = 0) { documentRepository.saveDocument(DocumentType.SMS.content, any()) }
    }
}
