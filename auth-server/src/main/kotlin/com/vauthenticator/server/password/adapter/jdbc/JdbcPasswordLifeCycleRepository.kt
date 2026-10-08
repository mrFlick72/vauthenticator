package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleAction
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRepository
import com.vauthenticator.server.password.domain.lifecycle.PasswordLifeCycleRule
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.time.Duration
import java.time.LocalDateTime

private const val UPSERT_RULE_QUERY = """
    INSERT INTO PASSWORD_LIFECYCLE_RULES (user_name, evaluation_interval_seconds, created_at, last_evaluation_date, action) VALUES (?,?,?,?,?)
    ON CONFLICT (user_name, action) DO UPDATE
    SET evaluation_interval_seconds = EXCLUDED.evaluation_interval_seconds, created_at = EXCLUDED.created_at, last_evaluation_date = EXCLUDED.last_evaluation_date
    """
private const val DELETE_RULE_QUERY = "DELETE FROM PASSWORD_LIFECYCLE_RULES WHERE user_name = ? AND action = ?"
private const val UPDATE_LAST_EVALUATION_DATE_QUERY =
    "UPDATE PASSWORD_LIFECYCLE_RULES SET last_evaluation_date = ? WHERE user_name = ? AND action = ?"
private const val FIND_RULES_FOR_USER_QUERY =
    "SELECT * FROM PASSWORD_LIFECYCLE_RULES WHERE user_name = ? ORDER BY action"
private const val FIND_FIRST_RULES_QUERY =
    "SELECT * FROM PASSWORD_LIFECYCLE_RULES ORDER BY user_name, action LIMIT ?"
private const val FIND_RULES_AFTER_QUERY =
    "SELECT * FROM PASSWORD_LIFECYCLE_RULES WHERE (user_name, action) > (?, ?) ORDER BY user_name, action LIMIT ?"

class JdbcPasswordLifeCycleRepository(private val jdbcTemplate: JdbcTemplate) : PasswordLifeCycleRepository {

    private val ruleMapper = RowMapper { rs, _ ->
        PasswordLifeCycleRule(
            userName = rs.getString("user_name"),
            interval = Duration.ofSeconds(rs.getLong("evaluation_interval_seconds")),
            creationDate = rs.getObject("created_at", LocalDateTime::class.java),
            lastEvaluationDate = rs.getObject("last_evaluation_date", LocalDateTime::class.java),
            action = PasswordLifeCycleAction.valueOf(rs.getString("action"))
        )
    }

    override fun store(rule: PasswordLifeCycleRule) {
        jdbcTemplate.update(
            UPSERT_RULE_QUERY,
            rule.userName, rule.interval.seconds, rule.creationDate, rule.lastEvaluationDate, rule.action.name
        )
    }

    override fun delete(userName: String, action: PasswordLifeCycleAction) {
        jdbcTemplate.update(DELETE_RULE_QUERY, userName, action.name)
    }

    override fun updateLastEvaluationDate(rule: PasswordLifeCycleRule, lastEvaluationDate: LocalDateTime) {
        jdbcTemplate.update(UPDATE_LAST_EVALUATION_DATE_QUERY, lastEvaluationDate, rule.userName, rule.action.name)
    }

    override fun findRulesFor(userName: String): List<PasswordLifeCycleRule> =
        jdbcTemplate.query(FIND_RULES_FOR_USER_QUERY, ruleMapper, userName)

    override fun findAllRulesAfter(after: PasswordLifeCycleRule?, size: Int): List<PasswordLifeCycleRule> =
        after?.let { jdbcTemplate.query(FIND_RULES_AFTER_QUERY, ruleMapper, it.userName, it.action.name, size) }
            ?: jdbcTemplate.query(FIND_FIRST_RULES_QUERY, ruleMapper, size)
}
