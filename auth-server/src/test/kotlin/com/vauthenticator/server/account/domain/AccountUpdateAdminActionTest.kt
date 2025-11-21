package com.vauthenticator.server.account.domain

import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class AccountUpdateAdminActionTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    lateinit var underTest: AccountUpdateAdminAction
    val anAccount = anAccount()
    val request = AdminAccountApiRequest(
        email = anAccount.email,
        enabled = true,
        accountLocked = true,
        authorities = setOf("A_ROLE"),
        mandatoryAction = AccountMandatoryAction.NO_ACTION
    )
    @BeforeEach
    fun setUp() {
        underTest = AccountUpdateAdminAction(accountRepository)
    }

    @Test
    fun `when we change account data`() {
        val account = anAccount.copy(accountNonLocked = false, enabled = true, authorities = setOf("A_ROLE"))
        every { accountRepository.accountFor(account.email) } returns Optional.of(anAccount())
        every { accountRepository.save(account) } just runs

        underTest.execute(request)

        verify { accountRepository.accountFor(account.email) }
        verify { accountRepository.save(account) }
    }

    @Test
    fun `when the account is not found`() {
        val account = anAccount.copy(accountNonLocked = false, enabled = true, authorities = setOf("A_ROLE"))
        every { accountRepository.accountFor(account.email) } returns Optional.empty()

        underTest.execute(request)

        verify { accountRepository.accountFor(account.email) }
        verify(exactly = 0) { accountRepository.save(account) }
    }
}