package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountPattern
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.role.domain.AdministrationRequiredException
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.AdministrationFixture.accountAdministrationFor
import com.vauthenticator.server.support.AdministrationFixture.anAdministrator
import com.vauthenticator.server.support.AdministrationFixture.notAnAdministrator
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
    private val anAdministratorAccount = anAccount().copy(username = "admin@email.com", email = "admin@email.com", authorities = setOf(Role.adminRole().name))

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository

    lateinit var uut: PasswordLifeCycleRules

    @BeforeEach
    fun setUp() {
        uut = PasswordLifeCycleRules(clock, accountRepository, accountAdministrationFor(notAnAdministrator), passwordLifeCycleRepository)
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
    fun `a caller that is not an administrator cannot register a rule for an administrator account`() {
        every { accountRepository.accountFor(anAdministratorAccount.username) } returns anAdministratorAccount

        assertThrows<AdministrationRequiredException> {
            uut.register(anAdministratorAccount.username, PasswordLifeCycleAction.ACCOUNT_LOCK, Duration.ofDays(90))
        }
        verify(exactly = 0) { passwordLifeCycleRepository.store(any()) }
    }

    @Test
    fun `an administrator registers a rule for an administrator account`() {
        uut = PasswordLifeCycleRules(clock, accountRepository, accountAdministrationFor(anAdministrator), passwordLifeCycleRepository)
        every { accountRepository.accountFor(anAdministratorAccount.username) } returns anAdministratorAccount
        every { passwordLifeCycleRepository.store(any()) } just runs

        uut.register(anAdministratorAccount.username, PasswordLifeCycleAction.ACCOUNT_LOCK, Duration.ofDays(90))

        verify { passwordLifeCycleRepository.store(match { it.userName == anAdministratorAccount.username }) }
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
    fun `a caller that is not an administrator cannot remove a rule from an administrator account`() {
        every { accountRepository.accountFor(anAdministratorAccount.username) } returns anAdministratorAccount

        assertThrows<AdministrationRequiredException> {
            uut.remove(anAdministratorAccount.username, PasswordLifeCycleAction.ACCOUNT_LOCK)
        }
        verify(exactly = 0) { passwordLifeCycleRepository.delete(any(), any()) }
    }

    @Test
    fun `when a rule is removed`() {
        every { accountRepository.accountFor(account.username) } returns account
        every { passwordLifeCycleRepository.delete(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK) } returns 1

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
        every { accountRepository.accountFor(any()) } answers { anAccount().copy(username = firstArg(), email = firstArg()) }
        every { passwordLifeCycleRepository.store(any()) } just runs

        val outcome = uut.registerAll(pattern, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(90))

        assertEquals(PasswordLifeCycleBulkOutcome(applied = 101, skipped = 0), outcome)
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
    fun `a caller that is not an administrator registers a rule for a pattern skipping the administrator accounts`() {
        val pattern = AccountPattern("*@email.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns listOf(anAdministratorAccount.username, account.username)
        every { accountRepository.findUserNamesMatching(pattern, account.username, 100) } returns emptyList()
        every { accountRepository.accountFor(anAdministratorAccount.username) } returns anAdministratorAccount
        every { accountRepository.accountFor(account.username) } returns account
        every { passwordLifeCycleRepository.store(any()) } just runs

        val outcome = uut.registerAll(pattern, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(90))

        assertEquals(PasswordLifeCycleBulkOutcome(applied = 1, skipped = 1), outcome)
        verify { passwordLifeCycleRepository.store(match { it.userName == account.username }) }
        verify(exactly = 0) { passwordLifeCycleRepository.store(match { it.userName == anAdministratorAccount.username }) }
    }

    @Test
    fun `an administrator registers a rule for a pattern on every matching account without loading them`() {
        uut = PasswordLifeCycleRules(clock, accountRepository, accountAdministrationFor(anAdministrator), passwordLifeCycleRepository)
        val pattern = AccountPattern("*@email.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns listOf(anAdministratorAccount.username, account.username)
        every { accountRepository.findUserNamesMatching(pattern, account.username, 100) } returns emptyList()
        every { passwordLifeCycleRepository.store(any()) } just runs

        val outcome = uut.registerAll(pattern, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(90))

        assertEquals(PasswordLifeCycleBulkOutcome(applied = 2, skipped = 0), outcome)
        verify(exactly = 0) { accountRepository.accountFor(any()) }
    }

    @Test
    fun `when a rule is registered for a pattern that matches no account`() {
        val pattern = AccountPattern("*@gmial.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns emptyList()

        assertEquals(
            PasswordLifeCycleBulkOutcome(applied = 0, skipped = 0),
            uut.registerAll(pattern, PasswordLifeCycleAction.PASSWORD_RESET, Duration.ofDays(90))
        )
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
        uut = PasswordLifeCycleRules(clock, accountRepository, accountAdministrationFor(anAdministrator), passwordLifeCycleRepository)
        val pattern = AccountPattern("*@gmail.com")
        val firstPage = (1..100).map { "user$it@gmail.com" }
        val secondPage = listOf("zed@gmail.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns firstPage
        every { accountRepository.findUserNamesMatching(pattern, firstPage.last(), 100) } returns secondPage
        every { accountRepository.findUserNamesMatching(pattern, "zed@gmail.com", 100) } returns emptyList()
        every { passwordLifeCycleRepository.delete(any(), PasswordLifeCycleAction.ACCOUNT_LOCK) } returns 0
        every { passwordLifeCycleRepository.delete("user1@gmail.com", PasswordLifeCycleAction.ACCOUNT_LOCK) } returns 1
        every { passwordLifeCycleRepository.delete("zed@gmail.com", PasswordLifeCycleAction.ACCOUNT_LOCK) } returns 1

        assertEquals(
            PasswordLifeCycleBulkOutcome(applied = 2, skipped = 0),
            uut.removeAll(pattern, PasswordLifeCycleAction.ACCOUNT_LOCK)
        )
        (firstPage + secondPage).forEach { verify { passwordLifeCycleRepository.delete(it, PasswordLifeCycleAction.ACCOUNT_LOCK) } }
    }

    @Test
    fun `a caller that is not an administrator removes the rules for a pattern skipping the administrator accounts`() {
        val pattern = AccountPattern("*@email.com")
        every { accountRepository.findUserNamesMatching(pattern, null, 100) } returns listOf(anAdministratorAccount.username, account.username)
        every { accountRepository.findUserNamesMatching(pattern, account.username, 100) } returns emptyList()
        every { accountRepository.accountFor(anAdministratorAccount.username) } returns anAdministratorAccount
        every { accountRepository.accountFor(account.username) } returns account
        every { passwordLifeCycleRepository.delete(account.username, PasswordLifeCycleAction.ACCOUNT_LOCK) } returns 1

        assertEquals(
            PasswordLifeCycleBulkOutcome(applied = 1, skipped = 1),
            uut.removeAll(pattern, PasswordLifeCycleAction.ACCOUNT_LOCK)
        )
        verify(exactly = 0) { passwordLifeCycleRepository.delete(anAdministratorAccount.username, any()) }
    }
}
