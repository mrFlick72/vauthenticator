package com.vauthenticator.server.password.adapter.jdbc

import com.vauthenticator.server.password.adapter.AbstractPasswordHistoryRepositoryTest
import com.vauthenticator.server.password.domain.PasswordHistoryRepository
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import java.time.Clock

class JdbcPasswordHistoryRepositoryTest : AbstractPasswordHistoryRepositoryTest() {
    override fun initPasswordHistoryRepository(): PasswordHistoryRepository =
        JdbcPasswordHistoryRepository(
            2,
            3,
            Clock.systemUTC(),
            jdbcTemplate
        )

    override fun resetDatabase() {
        resetDb()
    }

    override fun loadActualDynamoSizeFor(userName: String): List<Map<String, Any>> {
        return jdbcTemplate.query("SELECT * FROM PASSWORD_HISTORY WHERE user_name = ?", {rs,_ -> mapOf(
            "user_name" to rs.getString("user_name"),
            "created_at" to rs.getLong("created_at"),
            "password" to rs.getString("password")
        ) }, userName)
    }

}