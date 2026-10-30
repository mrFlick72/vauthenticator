package com.vauthenticator.server.ticket.domain

interface TicketRepository {
    fun store(ticket: Ticket)
    fun loadFor(ticketId: TicketId): Ticket?
    fun delete(ticketId: TicketId)

    /** Atomically counts one more code checked against the ticket; null when the ticket no longer exists. */
    fun recordCodeAttempt(ticketId: TicketId): Int?
}
