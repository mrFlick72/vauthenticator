package com.vauthenticator.server.ticket.adapter.jdbc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import com.vauthenticator.server.ticket.adapter.AbstractTicketRepositoryTest
import com.vauthenticator.server.ticket.domain.TicketRepository

class JdbcTicketRepositoryTest : AbstractTicketRepositoryTest() {

    override fun initTicketRepository(): TicketRepository =
        JdbcTicketRepository(jdbcTemplate, jacksonObjectMapper())


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