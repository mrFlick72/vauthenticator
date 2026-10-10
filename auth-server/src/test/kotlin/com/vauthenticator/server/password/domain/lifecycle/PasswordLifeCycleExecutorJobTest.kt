package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.AccountNotFoundException
import com.vauthenticator.server.support.passwordLifeCycleRule
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PasswordLifeCycleExecutorJobTest {

    @MockK
    private lateinit var passwordLifeCycleRepository: PasswordLifeCycleRepository

    @MockK
    private lateinit var passwordLifeCycleExecutor: PasswordLifeCycleExecutor

    lateinit var uut: PasswordLifeCycleExecutorJob

    private val user1Rule = passwordLifeCycleRule.copy(userName = "user_1")
    private val user2Rule = passwordLifeCycleRule.copy(userName = "user_2")
    private val user3Rule = passwordLifeCycleRule.copy(userName = "user_3")

    @BeforeEach
    fun setUp() {
        uut = PasswordLifeCycleExecutorJob(passwordLifeCycleRepository, passwordLifeCycleExecutor)
    }

    @Test
    fun `when rules are processed page by page after the last seen rule`() {
        every { passwordLifeCycleRepository.findAllRulesAfter(after = null, size = 100) } returns listOf(user1Rule, user2Rule)
        every { passwordLifeCycleRepository.findAllRulesAfter(after = user2Rule, size = 100) } returns listOf(user3Rule)
        every { passwordLifeCycleRepository.findAllRulesAfter(after = user3Rule, size = 100) } returns emptyList()
        every { passwordLifeCycleExecutor.execute(any()) } just runs

        uut.execute()

        verify(exactly = 1) { passwordLifeCycleExecutor.execute(user1Rule) }
        verify(exactly = 1) { passwordLifeCycleExecutor.execute(user2Rule) }
        verify(exactly = 1) { passwordLifeCycleExecutor.execute(user3Rule) }
    }

    @Test
    fun `when no rules are processed`() {
        every { passwordLifeCycleRepository.findAllRulesAfter(after = null, size = 100) } returns emptyList()

        uut.execute()

        verify(exactly = 0) { passwordLifeCycleExecutor.execute(any()) }
    }

    @Test
    fun `when a rule fails the other rules are still processed`() {
        every { passwordLifeCycleRepository.findAllRulesAfter(after = null, size = 100) } returns listOf(user1Rule, user2Rule)
        every { passwordLifeCycleRepository.findAllRulesAfter(after = user2Rule, size = 100) } returns emptyList()
        every { passwordLifeCycleExecutor.execute(user1Rule) } throws RuntimeException("boom")
        every { passwordLifeCycleExecutor.execute(user2Rule) } just runs

        uut.execute()

        verify(exactly = 1) { passwordLifeCycleExecutor.execute(user2Rule) }
        verify(exactly = 0) { passwordLifeCycleRepository.delete(any(), any()) }
    }

    @Test
    fun `when the account of a rule does not exist the rule is removed and the other rules are still processed`() {
        every { passwordLifeCycleRepository.findAllRulesAfter(after = null, size = 100) } returns listOf(user1Rule, user2Rule)
        every { passwordLifeCycleRepository.findAllRulesAfter(after = user2Rule, size = 100) } returns emptyList()
        every { passwordLifeCycleExecutor.execute(user1Rule) } throws AccountNotFoundException("missing")
        every { passwordLifeCycleExecutor.execute(user2Rule) } just runs
        every { passwordLifeCycleRepository.delete(user1Rule.userName, user1Rule.action) } just runs

        uut.execute()

        verify { passwordLifeCycleRepository.delete(user1Rule.userName, user1Rule.action) }
        verify(exactly = 1) { passwordLifeCycleExecutor.execute(user2Rule) }
    }
}
