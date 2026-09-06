package com.vauthenticator.server.communication.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.document.domain.Document
import com.vauthenticator.server.document.domain.DocumentRepository
import com.vauthenticator.server.document.domain.DocumentType
import com.vauthenticator.server.communication.domain.EMailTemplate
import com.vauthenticator.server.communication.domain.EMailType
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class EMailEndPointTest {

    lateinit var mockMvc: MockMvc

    @MockK
    lateinit var documentRepository: DocumentRepository

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
            EMailEndPoint(documentRepository, PermissionValidator(clientApplicationRepository))
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    fun `when an mfa mail template is retrieved`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MAIL_TEMPLATE_READER.content))
        val response = EMailTemplate(
            EMailType.MFA,
            "A_TEMPLATE"
        )

        every {
            documentRepository.loadDocument(
                DocumentType.EMAIL.content,
                EMailType.MFA.path
            )
        } returns Document(
            "",
            EMailType.MFA.path,
            "A_TEMPLATE".toByteArray()
        )

        mockMvc.perform(
            get("/api/email-template/${EMailType.MFA}")
                .principal(jwtAuthenticationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(response))
        )
            .andExpect(status().isOk)
            .andExpect(content().json(objectMapper.writeValueAsString(response)))
    }

    @Test
    fun `retrieving a mail template fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mockMvc.perform(
            get("/api/email-template/${EMailType.MFA}")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isForbidden)

        verify(exactly = 0) { documentRepository.loadDocument(DocumentType.EMAIL.content, EMailType.MFA.path) }
    }

    @Test
    fun `retrieving a mail template with admin full access scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.ADMIN_FULL_ACCESS.content))
        val response = EMailTemplate(
            EMailType.MFA,
            "A_TEMPLATE"
        )

        every {
            documentRepository.loadDocument(
                DocumentType.EMAIL.content,
                EMailType.MFA.path
            )
        } returns Document(
            "",
            EMailType.MFA.path,
            "A_TEMPLATE".toByteArray()
        )

        mockMvc.perform(
            get("/api/email-template/${EMailType.MFA}")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `when a new mail template is uploaded`() {
        val request = EMailTemplate(
            EMailType.WELCOME,
            "A_TEMPLATE"
        )

        every {
            documentRepository.saveDocument(
                DocumentType.EMAIL.content,
                Document(
                    "text/html",
                    "templates/welcome.html",
                    "A_TEMPLATE".toByteArray()
                )
            )
        } just runs

        mockMvc.perform(
            put("/api/email-template")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNoContent)
    }
}