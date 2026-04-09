package com.vauthenticator.server.ticket.domain

interface TicketRepository {
    fun store(ticket: Ticket)
    fun loadFor(ticketId: TicketId): Ticket?
    fun delete(ticketId: TicketId)
}
