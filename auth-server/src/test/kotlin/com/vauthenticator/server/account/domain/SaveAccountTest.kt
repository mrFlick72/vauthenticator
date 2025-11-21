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

        every { accountRepository.accountFor(account.email) } returns Optional.of(account)
        every { accountRepository.save(account) } just runs

        underTest.execute(principal, account)

        verify { accountRepository.save(account) }
    }

    @Test
    fun `when account is not found`() {
        val account = anAccount()
        val principal: JwtAuthenticationToken = SecurityFixture.principalFor("A_CLIENT_APP_ID", account.email)

        every { accountRepository.accountFor(account.email) } returns Optional.empty()

        underTest.execute(principal, account)

        verify(exactly = 0) { accountRepository.save(account) }
    }
}