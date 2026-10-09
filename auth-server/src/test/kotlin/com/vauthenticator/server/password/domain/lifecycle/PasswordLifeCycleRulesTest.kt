package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountPattern
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
import java.time.Duration
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

        uut.register(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK, Duration.ofDays(90))

        verify {
            passwordLifeCycleRepository.store(
                PasswordLifeCycleRule(
                    userName = account.username,
                    interval = Duration.ofDays(90),
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
            uut.register("unknown@email.com", PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(1))
        }
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `when a rule is registered with an interval beyond the supported date range`() {
        every { accountRepository.accountFor(account.username) } returns account

        assertThrows<InvalidPasswordLifeCycleIntervalException> {
            uut.register(account.username, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofSeconds(Long.MAX_VALUE))
        }
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `when a rule is registered with a non positive interval`() {
        listOf(Duration.ZERO, Duration.ofSeconds(-1)).forEach { interval ->
            assertThrows<InvalidPasswordLifeCycleIntervalException> {
                uut.register(account.username, PasswordLifeCycleAction.PASSWORD_RESET, interval)
            }
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

    @Test
    fun `when a rule is registered for every account matching a pattern`() {
        val pattern = AccountPattern("*@gmail.com")
        val firstPage = (1..100).map { "user$it@gmail.com" }
        val secondPage = listOf("zed@gmail.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns firstPage
        every { accountRepository.findUserNamesMatching(pattern, firstPage.last(), 100) } returns secondPage
        every { accountRepository.findUserNamesMatching(pattern, "zed@gmail.com", 100) } returns emptyList()
        every { passwordLifeCycleRepository.store(any()) } just runs

        val matched = uut.registerAll(pattern, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(90))

        assertEquals(101, matched)
        (firstPage + secondPage).forEach { userName ->
            verify {
                passwordLifeCycleRepository.store(
                    PasswordLifeCycleRule(
                        userName = userName,
                        interval = Duration.ofDays(90),
                        creationDate = LocalDateTime.of(2026, 10, 7, 10, 0, 0),
                        lastEvaluationDate = null,
                        action = PasswordLifeCycleAction.PASSWORD_RESET
                    )
                )
            }
        }
    }

    @Test
    fun `when a rule is registered for a pattern that matches no account`() {
        val pattern = AccountPattern("*@gmial.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns emptyList()

        assertEquals(0, uut.registerAll(pattern, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(90)))
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `when a rule is registered for a pattern with an invalid interval no account is searched`() {
        listOf(Duration.ZERO, Duration.ofSeconds(Long.MAX_VALUE)).forEach { interval ->
            assertThrows<InvalidPasswordLifeCycleIntervalException> {
                uut.registerAll(AccountPattern("*"), PasswordLifeCycleAction.ACCOUNT_LOCK, interval)
            }
        }
        verify(exactly = 0) { accountRepository.findUserNamesMatching(any(), any(), any()) }
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `when the rules of every account matching a pattern are removed`() {
        val pattern = AccountPattern("*@gmail.com")
        every { passwordLifeCycleRepository.deleteMatching(pattern, PasswordLifeCycleAction.ACCOUNT_LOCK) } returns 7

        assertEquals(7, uut.removeAll(pattern, PasswordLifeCycleAction.ACCOUNT_LOCK))
    }
}
