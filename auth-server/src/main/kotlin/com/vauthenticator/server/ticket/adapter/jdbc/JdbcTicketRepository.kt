package com.vauthenticator.server.ticket.adapter.jdbc
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketContext
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.springframework.jdbc.core.JdbcTemplate
import tools.jackson.databind.ObjectMapper
import java.time.Clock

class JdbcTicketRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
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


    override fun loadFor(ticketId: TicketId): Ticket? {
        val queryResult = jdbcTemplate.query(
            // an expired ticket is not redeemable even before the clean-up job deletes it
            "SELECT * FROM TICKET WHERE ticket = ? AND ttl >= ?",
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
            ticketId.content,
            clock.instant().epochSecond
        )

        return queryResult.firstOrNull()
    }

    override fun delete(ticketId: TicketId) {
        jdbcTemplate.update("DELETE FROM TICKET WHERE ticket=?", ticketId.content)
    }

    override fun recordFailedAttempt(ticketId: TicketId): Int? =
        jdbcTemplate.query(
            "UPDATE TICKET SET failed_attempts = failed_attempts + 1 WHERE ticket = ? RETURNING failed_attempts",
            { rs, _ -> rs.getInt("failed_attempts") },
            ticketId.content
        ).firstOrNull()

}
