package com.vauthenticator.server.account.domain

import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.SecurityFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*

@ExtendWith(MockKExtension::class)
class SaveAccountTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    lateinit var underTest: SaveAccount

    @BeforeEach
    fun setUp() {
        underTest = SaveAccount(accountRepository)
    }

    @Test
    fun `when account data are updated`() {
        val account = anAccount().copy(firstName = "A_NEW_FIRST_NAME")
        val principal: JwtAuthenticationToken = SecurityFixture.principalFor("A_CLIENT_APP_ID", anAccount().email)

        every { accountRepository.accountFor(account.email) } returns account
        every { accountRepository.save(account) } just runs

        underTest.execute(principal, account)

        verify { accountRepository.save(account) }
    }

    @Test
    fun `when account is not found`() {
        val account = anAccount()
        val principal: JwtAuthenticationToken = SecurityFixture.principalFor("A_CLIENT_APP_ID", account.email)

        every { accountRepository.accountFor(account.email) } returns null

        underTest.execute(principal, account)

        verify(exactly = 0) { accountRepository.save(account) }
    }

    @Test
    fun `when account data are updated identity and membership come from the stored account`() {
        val storedAccount = anAccount().copy(
            groups = setOf("A_STORED_GROUP"),
            authorities = setOf("A_STORED_ROLE"),
            mandatoryAction = AccountMandatoryAction.RESET_PASSWORD
        )
        val requestAccount = anAccount().copy(
            username = "",
            email = "",
            password = "A_REQUEST_PASSWORD",
            groups = setOf("A_REQUEST_GROUP"),
            authorities = setOf("A_REQUEST_ROLE"),
            mandatoryAction = AccountMandatoryAction.NO_ACTION,
            firstName = "A_NEW_FIRST_NAME",
            lastName = "A_NEW_LAST_NAME"
        )
        val principal: JwtAuthenticationToken = SecurityFixture.principalFor("A_CLIENT_APP_ID", storedAccount.email)
        val expected = storedAccount.copy(firstName = "A_NEW_FIRST_NAME", lastName = "A_NEW_LAST_NAME")

        every { accountRepository.accountFor(storedAccount.email) } returns storedAccount
        every { accountRepository.save(expected) } just runs

        underTest.execute(principal, requestAccount)

        verify { accountRepository.save(expected) }
    }

    @Test
    fun `when account data are updated for an email that is not the principal one`() {
        val storedAccount = anAccount()
        val requestAccount = anAccount().copy(username = "another@email.com", email = "another@email.com")
        val principal: JwtAuthenticationToken = SecurityFixture.principalFor("A_CLIENT_APP_ID", storedAccount.email)

        every { accountRepository.accountFor(storedAccount.email) } returns storedAccount

        assertThrows<AccountOwnershipException> { underTest.execute(principal, requestAccount) }

        verify(exactly = 0) { accountRepository.save(any()) }
    }
}
