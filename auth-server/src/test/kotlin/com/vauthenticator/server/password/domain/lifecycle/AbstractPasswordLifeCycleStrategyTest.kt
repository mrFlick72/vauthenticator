package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.PasswordLifeCycleActionAppliedEvent
import com.vauthenticator.server.events.VAuthenticatorEvent
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-12-20T10:15:30Z")
private val NOW_DATE_TIME = LocalDateTime.of(2026, 12, 20, 10, 15, 30)

@ExtendWith(MockKExtension::class)
abstract class AbstractPasswordLifeCycleStrategyTest {

    val clock: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository

    @MockK
    lateinit var eventsDispatcher: EventsDispatcher

    lateinit var uut: PasswordLifeCycleStrategy

    @BeforeEach
    fun setUp() {
        uut = BasePasswordLifeCycleStrategy(
            clock,
            accountRepository,
            passwordLifeCycleRepository,
            eventsDispatcher,
            initImplementation()
        )
        every { passwordLifeCycleRepository.updateLastEvaluationDate(any(), any()) } just runs
        every { passwordLifeCycleRepository.delete(any(), any()) } just runs
    }

    abstract fun initImplementation(): PasswordLifeCycleStrategyImplementation
    abstract fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction
    abstract fun accountWithoutActionInEffect(): Account
    abstract fun accountWithActionApplied(account: Account): Account

    private fun ruleCreatedAt(creationDate: LocalDateTime) = PasswordLifeCycleRule(
        anAccount().username,
        interval = PasswordLifeCycleInterval.parse("P1D"),
        creationDate = creationDate,
        lastEvaluationDate = null,
        action = passwordLifeCycleRuleAction()
    )

    @Test
    fun `when a due rule is executed the action is applied and an event is published`() {
        val account = accountWithoutActionInEffect()
        val rule = ruleCreatedAt(NOW_DATE_TIME.minusDays(6))
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.accountFor(account.username) } returns account
        every { accountRepository.save(accountWithActionApplied(account)) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        uut.execute(rule)

        verify { accountRepository.save(accountWithActionApplied(account)) }
        assertTrue(event.captured is PasswordLifeCycleActionAppliedEvent)
        assertEquals(account.username, event.captured.userName.content)
        assertEquals(ClientAppId.system(), event.captured.clientAppId)
        assertEquals(NOW, event.captured.timeStamp)
        assertEquals(passwordLifeCycleRuleAction(), event.captured.payload)
        verifyRuleAdvanced(rule)
        assertTrue(uut.canHandle(rule))
    }

    @Test
    fun `when a due rule finds the action already in effect nothing is saved or published but the rule advances`() {
        val account = accountWithActionApplied(accountWithoutActionInEffect())
        val rule = ruleCreatedAt(NOW_DATE_TIME.minusDays(6))
        every { accountRepository.accountFor(account.username) } returns account

        uut.execute(rule)

        verify(exactly = 0) { accountRepository.save(any()) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
        verifyRuleAdvanced(rule)
    }

    @Test
    fun `when a rule is not due nothing happens`() {
        val account = accountWithoutActionInEffect()
        val rule = ruleCreatedAt(NOW_DATE_TIME)
        every { accountRepository.accountFor(account.username) } returns account

        uut.execute(rule)

        verify(exactly = 0) { accountRepository.save(any()) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
        verify(exactly = 0) { passwordLifeCycleRepository.updateLastEvaluationDate(any(), any()) }
        verify(exactly = 0) { passwordLifeCycleRepository.delete(any(), any()) }
    }

    @Test
    fun `when the account of a rule does not exist`() {
        val rule = ruleCreatedAt(NOW_DATE_TIME.minusDays(6))
        every { accountRepository.accountFor(rule.userName) } returns null

        assertThrows<AccountNotFoundException> { uut.execute(rule) }
    }

    private fun verifyRuleAdvanced(rule: PasswordLifeCycleRule) {
        if (rule.action.oneShot) {
            verify { passwordLifeCycleRepository.delete(rule.userName, rule.action) }
            verify(exactly = 0) { passwordLifeCycleRepository.updateLastEvaluationDate(any(), any()) }
        } else {
            verify { passwordLifeCycleRepository.updateLastEvaluationDate(rule, NOW_DATE_TIME) }
            verify(exactly = 0) { passwordLifeCycleRepository.delete(any(), any()) }
        }
    }
}
