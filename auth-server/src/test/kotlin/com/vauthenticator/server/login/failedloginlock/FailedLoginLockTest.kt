package com.vauthenticator.server.login.failedloginlock

import com.vauthenticator.server.account.domain.AccountRepository
import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.FailedLoginLockAppliedEvent
import com.vauthenticator.server.events.FailedLoginLockReleasedEvent
import com.vauthenticator.server.events.VAuthenticatorEvent
import com.vauthenticator.server.oauth2.clientapp.domain.ClientAppId
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-10-25T10:15:30Z")
private val LOCK_DURATION: Duration = Duration.ofMinutes(1)
private const val THRESHOLD = 5L
private val CLIENT_APP_ID = ClientAppId("A_CLIENT_APP_ID")

@ExtendWith(MockKExtension::class)
class FailedLoginLockTest {

    private val clock: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var consecutiveFailedLogins: ConsecutiveFailedLogins

    @MockK
    lateinit var eventsDispatcher: EventsDispatcher

    lateinit var uut: FailedLoginLock

    @BeforeEach
    fun setUp() {
        uut = FailedLoginLock(
            accountRepository,
            consecutiveFailedLogins,
            eventsDispatcher,
            clock,
            FailedLoginLockConfig(threshold = THRESHOLD, duration = LOCK_DURATION)
        )
    }

    @Test
    fun `when consecutive failed logins reach the threshold the unlocked account gets a failed-login lock`() {
        val account = anAccount().copy(accountNonLocked = true)
        val lockedUntil = NOW.plus(LOCK_DURATION)
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.accountFor(account.username) } returns account
        every { consecutiveFailedLogins.increment(account.username, LOCK_DURATION) } returns THRESHOLD
        every { consecutiveFailedLogins.reset(account.username) } just runs
        every { accountRepository.save(any()) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        uut.failedLogin(account.username, CLIENT_APP_ID)

        verify { accountRepository.save(account.copy(accountNonLocked = false, lockedUntil = lockedUntil)) }
        verify { consecutiveFailedLogins.reset(account.username) }
        assertTrue(event.captured is FailedLoginLockAppliedEvent)
        assertEquals(account.username, event.captured.userName.content)
        assertEquals(CLIENT_APP_ID, event.captured.clientAppId)
        assertEquals(NOW, event.captured.timeStamp)
        assertEquals(FailedLoginLockApplied(lockedUntil), event.captured.payload)
    }

    @Test
    fun `when consecutive failed logins are below the threshold the account stays unlocked`() {
        val account = anAccount().copy(accountNonLocked = true)
        every { accountRepository.accountFor(account.username) } returns account
        every { consecutiveFailedLogins.increment(account.username, LOCK_DURATION) } returns THRESHOLD - 1

        uut.failedLogin(account.username, CLIENT_APP_ID)

        verify(exactly = 0) { accountRepository.save(any()) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when an already locked account fails a login it is not counted and its lock stays lasting`() {
        val account = anAccount().copy(accountNonLocked = false)
        every { accountRepository.accountFor(account.username) } returns account

        uut.failedLogin(account.username, CLIENT_APP_ID)

        verify(exactly = 0) { consecutiveFailedLogins.increment(any(), any()) }
        verify(exactly = 0) { accountRepository.save(any()) }
    }

    @Test
    fun `when the password is proven the consecutive failed logins are reset`() {
        every { consecutiveFailedLogins.reset(anAccount().username) } just runs

        uut.passwordProven(anAccount().username)

        verify { consecutiveFailedLogins.reset(anAccount().username) }
    }

    @Test
    fun `when the threshold is zero failed logins are not counted`() {
        val account = anAccount().copy(accountNonLocked = true)
        val disabled = FailedLoginLock(
            accountRepository,
            consecutiveFailedLogins,
            eventsDispatcher,
            clock,
            FailedLoginLockConfig(threshold = 0, duration = LOCK_DURATION)
        )
        every { accountRepository.accountFor(account.username) } returns account

        disabled.failedLogin(account.username, CLIENT_APP_ID)

        verify(exactly = 0) { consecutiveFailedLogins.increment(any(), any()) }
    }

    @Test
    fun `when a failed-login lock has expired the account is unlocked and the release is published`() {
        val lockedUntil = NOW.minusSeconds(1)
        val account = anAccount().copy(accountNonLocked = false, lockedUntil = lockedUntil)
        val unlocked = account.copy(accountNonLocked = true, lockedUntil = null)
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.save(unlocked) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertEquals(unlocked, actual)
        verify { accountRepository.save(unlocked) }
        assertTrue(event.captured is FailedLoginLockReleasedEvent)
        assertEquals(account.username, event.captured.userName.content)
        assertEquals(CLIENT_APP_ID, event.captured.clientAppId)
        assertEquals(NOW, event.captured.timeStamp)
        assertEquals(FailedLoginLockRelease(FailedLoginLockReleaseReason.EXPIRED, lockedUntil), event.captured.payload)
    }

    @Test
    fun `when a failed-login lock ends exactly now the account is unlocked`() {
        val account = anAccount().copy(accountNonLocked = false, lockedUntil = NOW)
        every { accountRepository.save(any()) } just runs
        every { eventsDispatcher.dispatch(any()) } just runs

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertTrue(actual.accountNonLocked)
    }

    @Test
    fun `when a failed-login lock has not expired yet the account stays locked`() {
        val account = anAccount().copy(accountNonLocked = false, lockedUntil = NOW.plusSeconds(1))

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertEquals(account, actual)
        verify(exactly = 0) { accountRepository.save(any()) }
    }

    @Test
    fun `when an account has a lasting lock it is never released`() {
        val account = anAccount().copy(accountNonLocked = false, lockedUntil = null)

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertEquals(account, actual)
        verify(exactly = 0) { accountRepository.save(any()) }
    }

    @Test
    fun `when a username with no account fails a login nothing is counted`() {
        every { accountRepository.accountFor("unknown@domain.com") } returns null

        uut.failedLogin("unknown@domain.com", CLIENT_APP_ID)

        verify(exactly = 0) { consecutiveFailedLogins.increment(any(), any()) }
    }
}
