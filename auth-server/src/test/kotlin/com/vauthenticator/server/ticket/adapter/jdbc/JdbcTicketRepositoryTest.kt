package com.vauthenticator.server.ticket.adapter.jdbc

import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.ticket.adapter.AbstractTicketRepositoryTest
import com.vauthenticator.server.ticket.domain.TicketRepository
import tools.jackson.databind.ObjectMapper

class JdbcTicketRepositoryTest : AbstractTicketRepositoryTest() {

    override fun initTicketRepository(): TicketRepository =
        JdbcTicketRepository(jdbcTemplate, ObjectMapper())


    override fun resetDatabase() {
        resetDb()
    }

    override fun getActual(): Map<String, Any> {
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