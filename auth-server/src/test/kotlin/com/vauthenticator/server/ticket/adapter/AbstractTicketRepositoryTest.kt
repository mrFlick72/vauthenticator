package com.vauthenticator.server.ticket.adapter

import com.vauthenticator.server.support.A_CLIENT_APP_ID
import com.vauthenticator.server.support.EMAIL
import com.vauthenticator.server.support.TicketFixture.ticketFor
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.util.*

// the ticket fixture expires at epoch second 200: tests run with the clock just before it
const val NOW_EPOCH_SECOND = 150L

abstract class AbstractTicketRepositoryTest {
    private val ticketValue = UUID.randomUUID().toString()
    private val ticketGenerator = { ticketValue }


    private lateinit var uut: TicketRepository
    private val ticket = ticketFor(ticketGenerator.invoke(), EMAIL, A_CLIENT_APP_ID)

    abstract fun initTicketRepository(): TicketRepository
    abstract fun resetDatabase()
    abstract fun getActual(): Map<String, String?>

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

        assertEquals(expected, actual)
    }

    @Test
    fun `when a ticket is expired it is not retrieved`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        uut.store(ticket.copy(ttl = NOW_EPOCH_SECOND - 1))

        assertNull(uut.loadFor(ticketId))
    }

    @Test
    fun `when a ticket expires this very second it is still retrieved`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        val expiringNow = ticket.copy(ttl = NOW_EPOCH_SECOND)
        uut.store(expiringNow)

        assertEquals(expiringNow, uut.loadFor(ticketId))
    }

    @Test
    fun `when a ticket is not present`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        val actual = uut.loadFor(ticketId)
        assertNull(actual)
    }

    @Test
    fun `when a ticket is delete`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        val expected = ticket

        uut.store(expected)

        val actual = uut.loadFor(ticketId)

        assertEquals(expected, actual)

        uut.delete(ticketId)
        val actualAfterDeletion = uut.loadFor(ticketId)

        assertNull(actualAfterDeletion)
    }

    @Test
    fun `when failed attempts are recorded on a ticket the count grows by one each time`() {
        val ticketId = TicketId(ticketGenerator.invoke())
        uut.store(ticket)

        assertEquals(1, uut.recordFailedAttempt(ticketId))
        assertEquals(2, uut.recordFailedAttempt(ticketId))
    }

    @Test
    fun `when a failed attempt is recorded on a missing ticket there is no count`() {
        assertNull(uut.recordFailedAttempt(TicketId(ticketGenerator.invoke())))
    }

    fun getTicketGenerator() = ticketGenerator

}