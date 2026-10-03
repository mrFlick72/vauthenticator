package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRepository
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Duration

class JdbcPasswordLifeCycleRepository(private val jdbcTemplate: JdbcTemplate) : PasswordLifeCycleRepository {
    override fun store(rule: PasswordLifeCycleRule) {
        jdbcTemplate.update(
            "INSERT INTO PASSWORD_LIFECYCLE_RULES (user_name, ttl, created_at, last_evaluation_date,action) VALUES (?,?,?,?,?)",
            rule.userName, rule.ttl.toSeconds(), rule.creationDate, rule.lastEvaluationDate, rule.action.name
        )
    }

    override fun delete(userName: String) {
        jdbcTemplate.update("DELETE FROM PASSWORD_LIFECYCLE_RULES WHERE user_name = ?", userName)
    }

    override fun findAllRules(page: Int, size: Int): List<PasswordLifeCycleRule> =
    jdbcTemplate.query("SELECT * FROM PASSWORD_LIFECYCLE_RULES LIMIT ? OFFSET ?", arrayOf(size, page * size))
    {
        rs, _ ->
        PasswordLifeCycleRule(
            userName = rs.getString("user_name"),
            ttl = Duration.ofSeconds(rs.getLong("ttl")),
            creationDate = rs.getObject("created_at", java.time.LocalDateTime::class.java),
            lastEvaluationDate = rs.getObject("last_evaluation_date", java.time.LocalDateTime::class.java),
            action = PasswordLifeCycleAction.valueOf(rs.getString("action"))
        )
    }
}