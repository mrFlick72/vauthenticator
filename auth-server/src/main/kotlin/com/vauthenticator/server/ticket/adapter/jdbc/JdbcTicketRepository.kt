package com.vauthenticator.server.ticket.adapter.jdbc

import com.fasterxml.jackson.databind.ObjectMapper
import com.vauthenticator.server.role.domain.Role
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.jdbc.core.JdbcTemplate
import java.util.*

class JdbcTicketRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper
) : TicketRepository {

    override fun store(ticket: Ticket) {
        jdbcTemplate.update(
            "INSERT INTO TICKET (ticket, ttl, user_name, client_application_id, context) VALUES (?,?,?,?,?)",
            ticket.ticketId.content,
            ticket.ttl,
            ticket.userName,
            ticket.clientAppId,
            objectMapper.writeValueAsString(ticket.context.content)
        )
    }


    override fun loadFor(ticketId: TicketId): Optional<Ticket> {
        val queryResult = jdbcTemplate.query(
            "SELECT * FROM TICKET WHERE ticket = ?",
            { rs, _ ->
                Ticket(
                    ticketId = TicketId(rs.getString("ticket")),
                    ttl = rs.getLong("ttl"),
                    userName = rs.getString("user_name"),
                    clientAppId = rs.getString("client_application_id"),
                    context = TicketContext(
                        objectMapper.readValue(
                            rs.getString("context"),
                            Map::class.java
                        ) as Map<String, String>
                    )

                )
            },
            ticketId.content
        )

        return if (queryResult.isEmpty()) {
            Optional.empty()
        } else {
            Optional.of(queryResult.first())
        }
    }

    override fun delete(ticketId: TicketId) {
        jdbcTemplate.update("DELETE FROM TICKET WHERE ticket=?", ticketId.content)
    }

}