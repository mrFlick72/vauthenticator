package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction.ACCOUNT_LOCK
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction.PASSWORD_RESET
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRepository
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.passwordLifeCycleRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

class JdbcPasswordLifeCycleRepositoryTest {

    lateinit var uut: PasswordLifeCycleRepository

    private val resetRule = passwordLifeCycleRule
    private val lockRule = passwordLifeCycleRule.copy(action = ACCOUNT_LOCK)

    @BeforeEach
    fun setUp() {
        resetDb()
        uut = JdbcPasswordLifeCycleRepository(jdbcTemplate)
    }

    @Test
    fun `when a new password lifecycle rule is stored`() {
        uut.store(resetRule)
        uut.store(lockRule)

        assertEquals(listOf(lockRule, resetRule), uut.findRulesFor(EMAIL))
    }

    @Test
    fun `when a rule for the same account and action is stored again it is replaced`() {
        uut.store(resetRule)
        uut.updateLastEvaluationDate(resetRule, LocalDateTime.of(2026, 10, 2, 10, 0, 0))

        val replacement = resetRule.copy(
            interval = Duration.ofDays(90),
            creationDate = LocalDateTime.of(2026, 10, 7, 10, 0, 0),
            lastEvaluationDate = null
        )
        uut.store(replacement)

        assertEquals(listOf(replacement), uut.findRulesFor(EMAIL))
    }

    @Test
    fun `when the last evaluation date of a password lifecycle rule is updated`() {
        val lastEvaluationDate = LocalDateTime.of(2026, 10, 6, 10, 0, 0)
        uut.store(resetRule)
        uut.store(lockRule)

        uut.updateLastEvaluationDate(resetRule, lastEvaluationDate)

        assertEquals(
            listOf(lockRule, resetRule.copy(lastEvaluationDate = lastEvaluationDate)),
            uut.findRulesFor(EMAIL)
        )
    }

    @Test
    fun `when a single rule of an account is deleted`() {
        uut.store(resetRule)
        uut.store(lockRule)

        uut.delete(EMAIL, ACCOUNT_LOCK)

        assertEquals(listOf(resetRule), uut.findRulesFor(EMAIL))
    }

    @Test
    fun `when all rules are read with keyset pagination`() {
        val rules = listOf("a@email.com", "b@email.com", "c@email.com")
            .flatMap { listOf(resetRule.copy(userName = it), lockRule.copy(userName = it)) }
        rules.shuffled().forEach { uut.store(it) }

        val firstPage = uut.findAllRulesAfter(after = null, size = 4)
        val secondPage = uut.findAllRulesAfter(after = firstPage.last(), size = 4)
        val thirdPage = uut.findAllRulesAfter(after = secondPage.last(), size = 4)

        assertEquals(4, firstPage.size)
        assertEquals(2, secondPage.size)
        assertEquals(emptyList<Any>(), thirdPage)
        assertEquals(rules.sortedWith(compareBy({ it.userName }, { it.action.name })), firstPage + secondPage)
    }

    @Test
    fun `when a rule already read is deleted the next page does not skip any rule`() {
        val rules = listOf("a@email.com", "b@email.com", "c@email.com").map { lockRule.copy(userName = it) }
        rules.forEach { uut.store(it) }

        val firstPage = uut.findAllRulesAfter(after = null, size = 1)
        uut.delete(firstPage.last().userName, ACCOUNT_LOCK)
        val secondPage = uut.findAllRulesAfter(after = firstPage.last(), size = 1)

        assertEquals(listOf(rules[1]), secondPage)
    }
}
