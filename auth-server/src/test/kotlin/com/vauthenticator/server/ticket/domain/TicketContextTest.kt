package com.vauthenticator.server.ticket.domain

import com.vauthenticator.server.mfa.domain.MfaDeviceId
import com.vauthenticator.server.mfa.domain.MfaMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TicketContextTest {

    private val mfaChannel = "user@example.com"
    private val mfaDeviceId = "A_MFA_DEVICE_ID"

    @Test
    fun `mfa method is loaded from a valid context`() {
        val ticketContext = validMfaContext()

        assertEquals(MfaMethod.EMAIL_MFA_METHOD, ticketContext.mfaMethod())
    }

    @Test
    fun `mfa channel is loaded from a valid context`() {
        val ticketContext = validMfaContext()

        assertEquals(mfaChannel, ticketContext.mfaChannel())
    }

    @Test
    fun `mfa device id is loaded from a valid context`() {
        val ticketContext = validMfaContext()

        assertEquals(MfaDeviceId(mfaDeviceId), ticketContext.mfaDeviceId())
    }

    @Test
    fun `missing mfa method makes the ticket invalid`() {
        val ticketContext = TicketContext(
            mapOf(
                Ticket.MFA_CHANNEL_CONTEXT_KEY to mfaChannel,
                Ticket.MFA_DEVICE_ID_CONTEXT_KEY to mfaDeviceId
            )
        )

        val actual = assertThrows(InvalidTicketException::class.java) { ticketContext.mfaMethod() }

        assertEquals("The ticket context is not valid, missing ${Ticket.MFA_METHOD_CONTEXT_KEY}", actual.message)
    }

    @Test
    fun `missing mfa channel makes the ticket invalid`() {
        val ticketContext = TicketContext(
            mapOf(
                Ticket.MFA_METHOD_CONTEXT_KEY to MfaMethod.EMAIL_MFA_METHOD.name,
                Ticket.MFA_DEVICE_ID_CONTEXT_KEY to mfaDeviceId
            )
        )

        val actual = assertThrows(InvalidTicketException::class.java) { ticketContext.mfaChannel() }

        assertEquals("The ticket context is not valid, missing ${Ticket.MFA_CHANNEL_CONTEXT_KEY}", actual.message)
    }

    @Test
    fun `missing mfa device id makes the ticket invalid`() {
        val ticketContext = TicketContext(
            mapOf(
                Ticket.MFA_METHOD_CONTEXT_KEY to MfaMethod.EMAIL_MFA_METHOD.name,
                Ticket.MFA_CHANNEL_CONTEXT_KEY to mfaChannel
            )
        )

        val actual = assertThrows(InvalidTicketException::class.java) { ticketContext.mfaDeviceId() }

        assertEquals("The ticket context is not valid, missing ${Ticket.MFA_DEVICE_ID_CONTEXT_KEY}", actual.message)
    }

    @Test
    fun `invalid mfa method makes the ticket invalid`() {
        val ticketContext = TicketContext(
            mapOf(
                Ticket.MFA_METHOD_CONTEXT_KEY to "NOT_A_METHOD",
                Ticket.MFA_CHANNEL_CONTEXT_KEY to mfaChannel,
                Ticket.MFA_DEVICE_ID_CONTEXT_KEY to mfaDeviceId
            )
        )

        val actual = assertThrows(InvalidTicketException::class.java) { ticketContext.mfaMethod() }

        assertEquals(
            "The ticket context is not valid, invalid ${Ticket.MFA_METHOD_CONTEXT_KEY} value: NOT_A_METHOD",
            actual.message
        )
    }

    private fun validMfaContext() = TicketContext.mfaContextFor(
        mfaMethod = MfaMethod.EMAIL_MFA_METHOD,
        mfaChannel = mfaChannel,
        mfaDeviceId = mfaDeviceId,
        ticketContextAdditionalProperties = emptyMap()
    )
}
