package com.vauthenticator.server.password.domain.resetpassword

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.ResetPasswordEvent
import com.vauthenticator.server.events.VAuthenticatorEventsDispatcher
import com.vauthenticator.server.password.api.ResetPasswordRequest
import com.vauthenticator.server.password.domain.PasswordPolicy
import com.vauthenticator.server.password.domain.VAuthenticatorPasswordEncoder
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.TicketFixture
import com.vauthenticator.server.ticket.domain.InvalidTicketException
import com.vauthenticator.server.ticket.domain.TicketId
import com.vauthenticator.server.ticket.domain.TicketRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class ResetAccountPasswordTest {

    lateinit var underTest: ResetAccountPassword

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var ticketRepository: TicketRepository

    @MockK
    lateinit var passwordPolicy: PasswordPolicy

    @MockK
    lateinit var vAuthenticatorPasswordEncoder: VAuthenticatorPasswordEncoder

    @MockK
    lateinit var eventsDispatcher: VAuthenticatorEventsDispatcher

    @BeforeEach
    fun setUp() {
        underTest =
            ResetAccountPassword(
                eventsDispatcher,
                accountRepository,
                vAuthenticatorPasswordEncoder,
                passwordPolicy,
                ticketRepository
            )
    }

    @Test
    fun `happy path`() {
        val anAccount = anAccount()
        val email = anAccount.email

        val ticketId = TicketId("A_TICKET")
        val ticket = TicketFixture.ticketFor(ticketId.content, email, "")

        every { passwordPolicy.accept(email, "NEW_PSWD") } just runs
        every { ticketRepository.loadFor(ticketId) } returns Optional.of(ticket)
        every { ticketRepository.delete(ticketId) } just runs
        every { accountRepository.accountFor(email) } returns Optional.of(anAccount)
        every { accountRepository.save(anAccount.copy(password = "NEW_PSWD")) } just runs
        every { vAuthenticatorPasswordEncoder.encode("NEW_PSWD") } returns "NEW_PSWD"
        every { eventsDispatcher.dispatch(any<ResetPasswordEvent>()) } just runs

        underTest.resetPasswordFromMailChallenge(ticketId, ResetPasswordRequest("NEW_PSWD"))
    }

    @Test
    fun `when a ticket was revoked`() {
        val ticketId = TicketId("A_TICKET")

        every { passwordPolicy.accept("A_USERNAME", "NEW_PSWD") } just runs
        every { ticketRepository.loadFor(ticketId) } returns Optional.empty()

        assertThrows(InvalidTicketException::class.java) {
            underTest.resetPasswordFromMailChallenge(
                ticketId,
                ResetPasswordRequest("NEW_PSWD")
            )
        }
    }
}