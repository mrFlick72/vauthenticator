package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.passwordLifeCycleRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleRulesTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-07T10:00:00Z"), ZoneOffset.UTC)
    private val account = anAccount()

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository

    lateinit var uut: PasswordLifeCycleRules

    @BeforeEach
    fun setUp() {
        uut = PasswordLifeCycleRules(clock, accountRepository, passwordLifeCycleRepository)
    }

    @Test
    fun `when a rule is registered the server sets the creation date and clears the last evaluation`() {
        every { accountRepository.accountFor(account.username) } returns account
        every { passwordLifeCycleRepository.store(any()) } just runs

        uut.register(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK, PasswordLifeCycleInterval.parse("P3M"))

        verify {
            passwordLifeCycleRepository.store(
                PasswordLifeCycleRule(
                    userName = account.username,
                    interval = PasswordLifeCycleInterval.parse("P3M"),
                    creationDate = LocalDateTime.of(2026, 10, 7, 10, 0, 0),
                    lastEvaluationDate = null,
                    action = PasswordLifeCycleAction.ACCOUNT_LOCK
                )
            )
        }
    }

    @Test
    fun `when a rule is registered for an unknown account`() {
        every { accountRepository.accountFor("unknown@email.com") } returns null

        assertThrows<AccountNotFoundException> {
            uut.register("unknown@email.com", PasswordLifeCycleAction.PASSWORD_RESET, PasswordLifeCycleInterval.parse("P1D"))
        }
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `when a rule is registered with an interval beyond the supported date range`() {
        every { accountRepository.accountFor(account.username) } returns account

        assertThrows<InvalidPasswordLifeCycleIntervalException> {
            uut.register(account.username, PasswordLifeCycleAction.PASSWORD_RESET, PasswordLifeCycleInterval.parse("P999999999Y"))
        }
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `when the rules of an account are retrieved`() {
        every { passwordLifeCycleRepository.findRulesFor(account.username) } returns listOf(passwordLifeCycleRule)

        assertEquals(listOf(passwordLifeCycleRule), uut.rulesFor(account.username))
    }

    @Test
    fun `when a rule is removed`() {
        every { passwordLifeCycleRepository.delete(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK) } just runs

        uut.remove(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK)

        verify { passwordLifeCycleRepository.delete(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK) }
    }
}
