package com.vauthenticator.server.ticket.adapter

import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.TicketFixture.ticketFor
import com.vauthenticator.server.ticket.domain.Ticket
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

abstract class AbstractTicketRepositoryTest {
    private val ticketValue = UUID.randomUUID().toString()
    private val ticketGenerator = { ticketValue }


    private lateinit var uut: TicketRepository
    private val ticket = ticketFor(ticketGenerator.invoke(), EMAIL, A_CLIENT_APP_ID)

    abstract fun initTicketRepository(): TicketRepository
    abstract fun resetDatabase()
    abstract fun getActual(): Map<String, Any>

    @BeforeEach
    fun setUp() {
        uut = initTicketRepository()
        resetDatabase()
    }

    @Test
    fun `when a ticket is stored`() {
        uut.store(ticket)

        val item = getActual()

        val ticketFromDynamo = item["ticket"]
        val ticketTTLFromDynamo = item["ttl"]

        assertEquals(ticketFromDynamo, ticket.ticketId.content)
        assertEquals(ticketTTLFromDynamo, "200")
    }



    @Test
    fun `when a ticket is retrieved`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        val expected = ticket

        uut.store(expected)

        val actual = uut.loadFor(ticketId)

        assertEquals(Optional.of(expected), actual)
    }

    @Test
    fun `when a ticket is not present`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        val actual = uut.loadFor(ticketId)
        assertEquals(Optional.empty<Ticket>(), actual)
    }

    @Test
    fun `when a ticket is delete`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        val expected = ticket

        uut.store(expected)

        val actual = uut.loadFor(ticketId)

        assertEquals(Optional.of(expected), actual)

        uut.delete(ticketId)
        val actualAfterDeletion = uut.loadFor(ticketId)

        assertEquals(Optional.empty<Ticket>(), actualAfterDeletion)
    }

    fun getTicketGenerator() = ticketGenerator

}