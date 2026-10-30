package com.vauthenticator.server.ticket.adapter.jdbc

import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.ticket.adapter.AbstractTicketRepositoryTest
import com.vauthenticator.server.ticket.adapter.NOW_EPOCH_SECOND
import com.vauthenticator.server.ticket.domain.TicketRepository
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class JdbcTicketRepositoryTest : AbstractTicketRepositoryTest() {

    override fun initTicketRepository(): TicketRepository =
        JdbcTicketRepository(jdbcTemplate, ObjectMapper(), Clock.fixed(Instant.ofEpochSecond(NOW_EPOCH_SECOND), ZoneOffset.UTC))


    override fun resetDatabase() {
        resetDb()
    }

    override fun getActual(): Map<String, String?> {
        val query = jdbcTemplate.query(
            "SELECT * FROM TICKET WHERE ticket = ?", { rs, _ ->
                mapOf(
                    "ticket" to rs.getString("ticket"),
                    "ttl" to rs.getString("ttl"),
                )
            },
            getTicketGenerator().invoke()
        )
        return if (query.isNotEmpty()) {
            query.first()
        } else {
            emptyMap()
        }
    }

}