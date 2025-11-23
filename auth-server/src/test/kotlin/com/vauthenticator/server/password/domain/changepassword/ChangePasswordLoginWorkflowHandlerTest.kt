package com.vauthenticator.server.password.domain.changepassword

import com.vauthenticator.server.account.domain.AccountMandatoryAction.RESET_PASSWORD
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.SecurityFixture.principalFor
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.util.*

@ExtendWith(MockKExtension::class)
class ChangePasswordLoginWorkflowHandlerTest {

    @MockK
    lateinit var handler: AuthenticationSuccessHandler

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var request: HttpServletRequest

    @MockK
    lateinit var response: HttpServletResponse


    private val account = AccountTestFixture.anAccount()

    @Test
    fun `when the change password is required`() {
        val account = account.copy(mandatoryAction = RESET_PASSWORD)
        val uut = ChangePasswordLoginWorkflowHandler(accountRepository, handler)

        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)

        val actual = uut.canHandle(request, response)

        verify { accountRepository.accountFor(account.email) }

        assertTrue(actual)
    }

    @Test
    fun `when the change password is not required`() {
        val uut = ChangePasswordLoginWorkflowHandler(accountRepository, handler)

        SecurityContextHolder.getContext().authentication = principalFor(account.email)
        every { accountRepository.accountFor(account.email) } returns Optional.of(account)

        val actual = uut.canHandle(request, response)

        verify { accountRepository.accountFor(account.email) }

        assertFalse(actual)
    }

}