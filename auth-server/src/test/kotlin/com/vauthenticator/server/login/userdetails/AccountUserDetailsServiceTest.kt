package com.vauthenticator.server.login.userdetails

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.VAuthenticatorEvent
import com.vauthenticator.server.login.failedloginlock.ConsecutiveFailedLogins
import com.vauthenticator.server.login.failedloginlock.FailedLoginLock
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockConfig
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.AccountTestFixture
import com.vauthenticator.server.web.CurrentHttpServletRequestService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.*

private val NOW: Instant = Instant.parse("2026-10-25T10:15:30Z")

@ExtendWith(MockKExtension::class)
class AccountUserDetailsServiceTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var consecutiveFailedLogins: ConsecutiveFailedLogins

    @MockK
    lateinit var eventsDispatcher: EventsDispatcher

    @MockK
    lateinit var currentHttpServletRequestService: CurrentHttpServletRequestService

    private val account = AccountTestFixture.anAccount()

    private lateinit var uut: AccountUserDetailsService

    @BeforeEach
    fun setUp() {
        val failedLoginLock = FailedLoginLock(
            accountRepository,
            consecutiveFailedLogins,
            eventsDispatcher,
            Clock.fixed(NOW, ZoneOffset.UTC),
            FailedLoginLockConfig()
        )
        uut = AccountUserDetailsService(accountRepository, failedLoginLock, currentHttpServletRequestService)
    }

    @Test
    fun `when the failed-login lock of the account has expired the user is no longer locked`() {
        val locked = account.copy(accountNonLocked = false, lockedUntil = NOW.minusSeconds(1))
        val event = slot<VAuthenticatorEvent>()
        val request = MockHttpServletRequest().apply { addParameter("client_id", "A_CLIENT_APP_ID") }
        every { accountRepository.accountFor(account.username) } returns locked
        every { accountRepository.save(any()) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs
        every { currentHttpServletRequestService.getServletRequest() } returns request

        val actual = uut.loadUserByUsername(account.username)

        assertTrue(actual.isAccountNonLocked)
        assertEquals(ClientAppId("A_CLIENT_APP_ID"), event.captured.clientAppId)
    }

    @Test
    fun `when the failed-login lock of the account has not expired the user is still locked`() {
        val locked = account.copy(accountNonLocked = false, lockedUntil = NOW.plusSeconds(1))
        every { accountRepository.accountFor(account.username) } returns locked
        every { currentHttpServletRequestService.getServletRequest() } returns MockHttpServletRequest()

        val actual = uut.loadUserByUsername(account.username)

        assertFalse(actual.isAccountNonLocked)
    }

    @Test
    fun `when the login has no client app the release is attributed to the system client`() {
        val locked = account.copy(accountNonLocked = false, lockedUntil = NOW.minusSeconds(1))
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.accountFor(account.username) } returns locked
        every { accountRepository.save(any()) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs
        every { currentHttpServletRequestService.getServletRequest() } returns MockHttpServletRequest()

        uut.loadUserByUsername(account.username)

        assertEquals(ClientAppId.system(), event.captured.clientAppId)
    }

    @Test
    fun `happy path`() {
        val expected = User(
            account.username,
            account.password,
            account.enabled,
            account.accountNonExpired,
            account.credentialsNonExpired,
            account.accountNonLocked,
            account.authorities.map { SimpleGrantedAuthority(it) }
        )
        every { accountRepository.accountFor(account.username) } returns account

        val actual = uut.loadUserByUsername(account.username)
        assertEquals(expected, actual)
    }

    @Test
    fun `when the user does not exist`() {
        every { accountRepository.accountFor(account.username) } returns null

        assertThrows(UsernameNotFoundException::class.java) { uut.loadUserByUsername(account.username) }
    }
}