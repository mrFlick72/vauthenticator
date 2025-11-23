package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.extentions.expirationTimeStampInSecondFromNow
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.support.ClientAppFixture
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

private const val TICKET_ID = "A_TICKET_ID"

@ExtendWith(MockKExtension::class)
class TicketCreatorTest {
    private val ticketGenerator = { TICKET_ID }
    private val now = Instant.now()
    private val clock: Clock = Clock.fixed(now, ZoneId.systemDefault())

    @MockK
    private lateinit var ticketRepository: TicketRepository

    private val ticketFeatures: TicketFeatures = TicketFeatures(Duration.ofSeconds(100))
    private val clientAppId = ClientAppFixture.aClientAppId()
    private val account = AccountTestFixture.anAccount()
    private val ticket = Ticket(
        TicketId(TICKET_ID),
        account.email,
        clientAppId.content,
        ticketFeatures.ttl.expirationTimeStampInSecondFromNow(clock),
        TicketContext.empty()
    )

    @Test
    fun `happy path`() {

        val uut = TicketCreator(ticketGenerator, clock, ticketRepository, ticketFeatures)

        every { ticketRepository.store(ticket) } just runs

        val createTicketFor =
            uut.createTicketFor(account, clientAppId, TicketContext.empty())

        Assertions.assertEquals(TICKET_ID, createTicketFor.content)
    }
}