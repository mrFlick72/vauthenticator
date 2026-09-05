package com.vauthenticator.server.account.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.AccountUpdateAdminAction
import com.vauthenticator.server.account.domain.AdminAccountApiRequest
import com.vauthenticator.server.oauth2.clientapp.domain.ClientApplicationRepository
import com.vauthenticator.server.oauth2.clientapp.domain.Scope
import com.vauthenticator.server.role.domain.PermissionValidator
import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.AccountTestFixture
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
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
internal class AdminApiAccountEndPointTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var clientApplicationRepository: ClientApplicationRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(
            AdminApiAccountEndPoint(
                accountRepository,
                AccountUpdateAdminAction(accountRepository),
                PermissionValidator(clientApplicationRepository)
            )
        ).setControllerAdvice(ExceptionAdviceController()).build()
    }

    @Test
    internal fun `find an account by email`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.READ_ACCOUNT.content))

        every { accountRepository.accountFor("anemail@domain.com") } returns AccountTestFixture.anAccount()

        mokMvc.perform(
            get("/api/admin/accounts/anemail@domain.com/email")
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isOk)

        verify { accountRepository.accountFor("anemail@domain.com") }
    }

    @Test
    internal fun `find an account by email fails for insufficient scope`() {
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            get("/api/admin/accounts/anemail@domain.com/email")
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { accountRepository.accountFor(any()) }
    }

    @Test
    internal fun `set an account as disabled`() {
        val representation = AdminAccountApiRequest(email = "anemail@domain.com", enabled = false)
        val masterAccount = AccountTestFixture.anAccount().copy(enabled = false)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_ACCOUNT.content))

        every { accountRepository.accountFor("anemail@domain.com") } returns AccountTestFixture.anAccount()
        every { accountRepository.save(masterAccount) } just runs

        mokMvc.perform(
            put("/api/admin/accounts")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(representation))
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)

        verify { accountRepository.save(masterAccount) }
    }

    @Test
    internal fun `set an account as disabled fails for insufficient scope`() {
        val representation = AdminAccountApiRequest(email = "anemail@domain.com", enabled = false)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.MFA_ENROLLMENT.content))

        mokMvc.perform(
            put("/api/admin/accounts")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(representation))
                .principal(jwtAuthenticationToken)
        ).andExpect(status().isForbidden)

        verify(exactly = 0) { accountRepository.save(any()) }
    }

    @Test
    internal fun `when the account is not found`() {
        val representation = AdminAccountApiRequest(email = "anemail@domain.com", enabled = false)
        val jwtAuthenticationToken = m2mPrincipalFor(A_CLIENT_APP_ID, listOf(Scope.SAVE_ACCOUNT.content))

        every { accountRepository.accountFor("anemail@domain.com") } returns null

        mokMvc.perform(
            put("/api/admin/accounts")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(representation))
                .principal(jwtAuthenticationToken)
        )
            .andExpect(status().isNoContent)
    }
}
