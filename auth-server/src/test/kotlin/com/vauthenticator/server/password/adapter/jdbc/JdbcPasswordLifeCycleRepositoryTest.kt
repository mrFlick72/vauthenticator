package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRepository
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.support.passwordLifeCycleRule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class JdbcPasswordLifeCycleRepositoryTest {

    lateinit var uut: PasswordLifeCycleRepository

    @BeforeEach
    fun setUp() {
        resetDb()
        uut = JdbcPasswordLifeCycleRepository(jdbcTemplate)
    }

    @Test
    fun `when a new password lifecycle policy is stored`() {
        uut.store(passwordLifeCycleRule)
        uut.store(passwordLifeCycleRule.copy(action = PasswordLifeCycleAction.ACCOUNT_LOCK))

        uut.findAllRules(0, 10).let { rules ->
            assertEquals(2, rules.size)
            assertTrue(rules.any { it.action == PasswordLifeCycleAction.PASSWORD_RESET })
            assertTrue(rules.any { it.action == PasswordLifeCycleAction.ACCOUNT_LOCK })
        }

    }

    @Test
    fun `when passwords lifecycle policy for a user is deleted retrieved`() {
        uut.store(passwordLifeCycleRule)
        uut.store(passwordLifeCycleRule.copy(action = PasswordLifeCycleAction.ACCOUNT_LOCK))

        uut.findAllRules(0, 10).let { rules ->
            assertEquals(2, rules.size)
            assertTrue(rules.any { it.action == PasswordLifeCycleAction.PASSWORD_RESET })
            assertTrue(rules.any { it.action == PasswordLifeCycleAction.ACCOUNT_LOCK })
        }

        uut.delete(EMAIL)

        retrieve(EMAIL).let { rules ->
            assertEquals(0, rules.size)
        }

    }

    private fun retrieve(userName: String): List<PasswordLifeCycleRule> =
        jdbcTemplate.query("SELECT * FROM PASSWORD_LIFECYCLE_RULES WHERE user_name = ?", arrayOf(userName)) { rs, _ ->
            PasswordLifeCycleRule(
                userName = rs.getString("user_name"),
                ttl = Duration.ofSeconds(rs.getLong("ttl")),
                creationDate = rs.getObject("created_at", java.time.LocalDateTime::class.java),
                lastEvaluationDate = rs.getObject("last_evaluation_date", java.time.LocalDateTime::class.java),
                action = PasswordLifeCycleAction.valueOf(rs.getString("action"))
            )
        }
}