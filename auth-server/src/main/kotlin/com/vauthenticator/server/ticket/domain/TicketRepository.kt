package com.vauthenticator.server.ticket.domain

interface TicketRepository {
    fun store(ticket: Ticket)
    fun loadFor(ticketId: TicketId): Ticket?
    fun delete(ticketId: TicketId)

    /** Atomically counts one more failed attempt on the ticket; null when the ticket no longer exists. */
    fun recordFailedAttempt(ticketId: TicketId): Int?
}
