package com.vauthenticator.server.ticket.domain

import java.util.*

interface TicketRepository {
    fun store(ticket: Ticket)
    fun loadFor(ticketId: TicketId): Optional<Ticket>
    fun delete(ticketId: TicketId)
}

