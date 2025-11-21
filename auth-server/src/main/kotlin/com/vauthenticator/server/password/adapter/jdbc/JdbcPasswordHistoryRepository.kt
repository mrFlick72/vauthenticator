package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.domain.Password
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset

class JdbcPasswordHistoryRepository(
    private val historyEvaluationLimit: Int,
    private val maxHistoryAllowedSize: Int,
    private val clock: Clock,
    private val jdbcTemplate: JdbcTemplate
) : PasswordHistoryRepository {
    override fun store(userName: String, password: Password) {
        jdbcTemplate.update(
            "INSERT INTO PASSWORD_HISTORY (user_name,created_at,password) VALUES (?,?,?)",
            userName, createdAt(), password.content
        )
    }

    private fun createdAt() =
        LocalDateTime.now(clock)
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

    override fun load(userName: String): List<Password> {
        val items = jdbcTemplate.query(
            "SELECT * FROM PASSWORD_HISTORY WHERE user_name=? ORDER BY created_at DESC",
            { rs, _ ->
                mapOf(
                    "user_name" to rs.getString("user_name"),
                    "created_at" to rs.getLong("created_at"),
                    "password" to rs.getString("password")
                )
            },
            userName
        )
        val allowedPassword = items.take(historyEvaluationLimit)

        deleteUselessPasswordHistory(items)
        return allowedPassword.map { Password(it["password"]!! as String) }
    }

    private fun deleteUselessPasswordHistory(itemsInTheHistory: List<Map<String, Any>>) {
        val leftoverSize = itemsInTheHistory.size - maxHistoryAllowedSize
        if (leftoverSize > 0) {
            itemsInTheHistory.takeLast(leftoverSize)
                .forEach { itemToDelete ->
                    jdbcTemplate.update(
                        "DELETE FROM PASSWORD_HISTORY WHERE user_name=? AND created_at=?",
                        *itemToDelete.filterKeys { it != "password" }.values.toTypedArray()
                    )

                }
        }
    }
}