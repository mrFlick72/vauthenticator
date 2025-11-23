package com.vauthenticator.server.account.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.AccountUpdateAdminAction
import com.vauthenticator.server.account.domain.AdminAccountApiRequest
import com.vauthenticator.server.support.AccountTestFixture
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

@ExtendWith(MockKExtension::class)
internal class AdminApiAccountEndPointTest {

    lateinit var mokMvc: MockMvc

    @MockK
    lateinit var accountRepository: AccountRepository

    private val objectMapper = ObjectMapper()

    @BeforeEach
    internal fun setUp() {
        mokMvc = MockMvcBuilders.standaloneSetup(
            AdminApiAccountEndPoint(
                accountRepository,
                AccountUpdateAdminAction(accountRepository)
            )
        ).build()
    }

    @Test
    internal fun `set an account as disabled`() {
        val representation = AdminAccountApiRequest(email = "anemail@domain.com", enabled = false)
        val masterAccount = AccountTestFixture.anAccount().copy(enabled = false)

        every { accountRepository.accountFor("anemail@domain.com") } returns Optional.of(AccountTestFixture.anAccount())
        every { accountRepository.save(masterAccount) } just runs

        mokMvc.perform(
            put("/api/admin/accounts")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(representation))
        )
            .andExpect(status().isNoContent)

        verify { accountRepository.save(masterAccount) }
    }

    @Test
    internal fun `when the account is not found`() {
        val representation = AdminAccountApiRequest(email = "anemail@domain.com", enabled = false)

        every { accountRepository.accountFor("anemail@domain.com") } returns Optional.empty()

        mokMvc.perform(
            put("/api/admin/accounts")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(representation))
        )
            .andExpect(status().isNoContent)
    }
}