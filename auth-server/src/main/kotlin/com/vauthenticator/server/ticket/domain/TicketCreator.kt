package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.extentions.expirationTimeStampInSecondFromNow
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import java.time.Clock


class TicketCreator(
    private val ticketGenerator: () -> String,
    private val clock: Clock,
    private val ticketRepository: TicketRepository,
    private val ticketFeatures: TicketFeatures
) {
    fun createTicketFor(
        account: Account,
        clientAppId: ClientAppId,
        ticketContext: TicketContext = TicketContext.empty()
    ): TicketId {
        val ticketId = TicketId(ticketGenerator.invoke())
        val ticket = Ticket(
            ticketId,
            account.email,
            clientAppId.content,
            ticketFeatures.ttl.expirationTimeStampInSecondFromNow(clock),
            ticketContext
        )
        ticketRepository.store(ticket)
        return ticketId
    }

}