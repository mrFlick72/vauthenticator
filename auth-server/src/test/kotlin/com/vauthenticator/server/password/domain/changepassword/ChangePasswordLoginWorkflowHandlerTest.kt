package com.vauthenticator.server.password.domain.changepassword

import com.vauthenticator.server.account.domain.AccountMandatoryAction.RESET_PASSWORD
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ChangePasswordLoginWorkflowHandlerTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    private val account = AccountTestFixture.anAccount()

    lateinit var uut: ChangePasswordLoginWorkflowHandler

    @BeforeEach
    fun setUp() {
        uut = ChangePasswordLoginWorkflowHandler(accountRepository)
    }

    @Test
    fun `when the change password is required`() {
        every { accountRepository.accountFor(account.email) } returns account.copy(mandatoryAction = RESET_PASSWORD)

        assertTrue(uut.isRequiredFor(account.email, null))
    }

    @Test
    fun `when the change password is not required`() {
        every { accountRepository.accountFor(account.email) } returns account

        assertFalse(uut.isRequiredFor(account.email, null))
    }

    @Test
    fun `when the account does not exist the change password is not required`() {
        every { accountRepository.accountFor(account.email) } returns null

        assertFalse(uut.isRequiredFor(account.email, null))
    }

}
