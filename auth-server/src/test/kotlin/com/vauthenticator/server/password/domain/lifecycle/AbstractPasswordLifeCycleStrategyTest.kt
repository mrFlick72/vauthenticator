package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val NOW = "2026-12-20T10:15:30Z"
private val NOW_DATE_TIME = LocalDateTime.of(2026, 12, 20, 10, 15, 30)

@ExtendWith(MockKExtension::class)
abstract class AbstractPasswordLifeCycleStrategyTest {

    val clock: Clock = Clock.fixed(NOW.toInstant(), java.time.ZoneOffset.UTC)

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository

    lateinit var uut: PasswordLifeCycleStrategy

    @BeforeEach
    fun setUp() {
        uut = initPasswordLifeCycleStrategy()
    }

    abstract fun initPasswordLifeCycleStrategy() : PasswordLifeCycleStrategy
    abstract fun newAccountFrom(account: Account): Account

    abstract fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction

    fun accountRepository(): AccountRepository {
        return accountRepository
    }

    fun passwordLifeCycleRepository(): PasswordLifeCycleRepository {
        return passwordLifeCycleRepository
    }

    @Test
    fun `when a password reset rule is executed`() {
        val creationDate = "2026-12-14T10:15:30Z"

        val anAccount = anAccount().copy(accountNonLocked = true)

        val rule = PasswordLifeCycleRule(
            anAccount.username,
            ttl = Duration.ofDays(1),
            creationDate = LocalDateTime.parse(creationDate, DateTimeFormatter.ISO_DATE_TIME),
            lastEvaluationDate = null,
            action = passwordLifeCycleRuleAction()
        )
        every { accountRepository.accountFor(anAccount.username) } returns anAccount
        every { accountRepository.save(newAccountFrom(anAccount)) } just runs
        every { passwordLifeCycleRepository.updateLastEvaluationDate(rule, NOW_DATE_TIME) } just runs


        uut.execute(rule)


        verify { accountRepository.accountFor(anAccount.username) }
        verify { accountRepository.save(newAccountFrom(anAccount)) }
        verify { passwordLifeCycleRepository.updateLastEvaluationDate(rule, NOW_DATE_TIME) }

        assertTrue(uut.canHandle(rule))
    }

    @Test
    fun `when a password reset rule is not executed, ttl has not expired`() {
        val creationDate = "2026-12-20T10:15:30Z"

        val anAccount = anAccount().copy(accountNonLocked = true)

        val rule = PasswordLifeCycleRule(
            anAccount.username,
            ttl = Duration.ofDays(1),
            creationDate = LocalDateTime.parse(creationDate, DateTimeFormatter.ISO_DATE_TIME),
            lastEvaluationDate = null,
            action = passwordLifeCycleRuleAction()
        )
        every { accountRepository.accountFor(anAccount.username) } returns anAccount


        uut.execute(rule)


        verify { accountRepository.accountFor(anAccount.username) }
        verify(exactly = 0) { accountRepository.save(newAccountFrom(anAccount)) }
        verify(exactly = 0) { passwordLifeCycleRepository.updateLastEvaluationDate(any(), any()) }

        assertTrue(uut.canHandle(rule))
    }
}

private fun String.toInstant(): Instant {
    return Instant.parse(this)
}
