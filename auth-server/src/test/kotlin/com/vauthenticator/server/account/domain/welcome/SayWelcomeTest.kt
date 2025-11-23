package com.vauthenticator.server.account.domain.welcome

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.account.domain.welcome.SayWelcome
import com.vauthenticator.server.communication.domain.EMailSenderService
import com.vauthenticator.server.support.AccountTestFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class SayWelcomeTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var welcomeMailSender: EMailSenderService

    lateinit var underTest: SayWelcome

    @BeforeEach
    internal fun setUp() {
        underTest = SayWelcome(accountRepository, welcomeMailSender)
    }

    @Test
    internal fun `happy path`() {
        val email = "email@domain.com"
        val anAccount = AccountTestFixture.anAccount()
        every { accountRepository.accountFor(email) } returns Optional.of(anAccount)
        every { welcomeMailSender.sendFor(anAccount, emptyMap()) } just runs

        underTest.welcome(email)

        verify { welcomeMailSender.sendFor(anAccount, emptyMap()) }
    }

    @Test
    internal fun `sed mail for a non registered account`() {
        val email = "email@domain.com"
        val anAccount = AccountTestFixture.anAccount()
        every { accountRepository.accountFor(email) } returns Optional.empty()

        assertThrows(AccountNotFoundException::class.java) { underTest.welcome(email) }

        verify(exactly = 0) { welcomeMailSender.sendFor(anAccount, emptyMap()) }
    }
}