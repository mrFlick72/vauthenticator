package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountMandatoryAction
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val NOW = "2026-12-20T10:15:30Z"

@ExtendWith(MockKExtension::class)
class PasswordResetPasswordLifeCycleStrategyTest {

    val clock = Clock.fixed(NOW.toInstant(), java.time.ZoneOffset.UTC)

    @MockK
    lateinit var accountRepository: AccountRepository

    lateinit var uut: PasswordResetPasswordLifeCycleStrategy

    @BeforeEach
    fun setUp() {
        uut = PasswordResetPasswordLifeCycleStrategy(clock, accountRepository)
    }

    @Test
    fun `when a password reset rule is executed`() {
        val creationDate = "2026-12-14T10:15:30Z"

        val anAccount = anAccount()

        val rule = PasswordLifeCycleRule(
            anAccount.username,
            ttl = Duration.ofDays(1),
            creationDate = LocalDateTime.parse(creationDate, DateTimeFormatter.ISO_DATE_TIME),
            lastEvaluationDate = null,
            action = PasswordLifeCycleAction.PASSWORD_RESET
        )
        every { accountRepository.accountFor(anAccount.username) } returns anAccount
        every { accountRepository.save(anAccount.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD)) } just runs


        uut.execute(rule)


        verify { accountRepository.accountFor(anAccount.username) }
        verify { accountRepository.save(anAccount.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD)) }

        assertTrue(uut.canHandle(rule))
    }

    @Test
    fun `when a password reset rule is not executed, ttl has not expired`() {
        val creationDate = "2026-12-20T10:15:30Z"

        val anAccount = anAccount()

        val rule = PasswordLifeCycleRule(
            anAccount.username,
            ttl = Duration.ofDays(1),
            creationDate = LocalDateTime.parse(creationDate, DateTimeFormatter.ISO_DATE_TIME),
            lastEvaluationDate = null,
            action = PasswordLifeCycleAction.PASSWORD_RESET
        )
        every { accountRepository.accountFor(anAccount.username) } returns anAccount


        uut.execute(rule)


        verify { accountRepository.accountFor(anAccount.username) }
        verify(exactly = 0) { accountRepository.save(anAccount.copy(mandatoryAction = AccountMandatoryAction.RESET_PASSWORD)) }

        assertTrue(uut.canHandle(rule))
    }
}

private fun String.toInstant(): Instant {
    return Instant.parse(this)
}
