package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.mfa.domain.MfaDeviceId

interface TicketRepository {
    fun store(ticket: Ticket)
    fun loadFor(ticketId: TicketId): Ticket?
    fun delete(ticketId: TicketId)

    /** Deletes the account's pending MFA enrollment tickets of one device, so a new enrollment replaces them. */
    fun deleteMfaEnrollmentTicketsFor(userName: String, mfaDeviceId: MfaDeviceId)

    /** Atomically counts one more code checked against the ticket; null when the ticket no longer exists. */
    fun recordCodeAttempt(ticketId: TicketId): Int?
}
