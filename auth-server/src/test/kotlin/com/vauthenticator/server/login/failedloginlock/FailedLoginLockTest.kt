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
import org.junit.jupiter.api.Assertions.assertFalse
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
    lateinit var failedLoginLockStore: FailedLoginLockStore

    @MockK
    lateinit var consecutiveFailedLogins: ConsecutiveFailedLogins

    @MockK
    lateinit var eventsDispatcher: EventsDispatcher

    lateinit var uut: FailedLoginLock

    private val unlocked = anAccount().unlock()

    @BeforeEach
    fun setUp() {
        uut = failedLoginLockWith(FailedLoginLockConfig(threshold = THRESHOLD, duration = LOCK_DURATION))
    }

    private fun failedLoginLockWith(config: FailedLoginLockConfig) = FailedLoginLock(
        accountRepository,
        failedLoginLockStore,
        consecutiveFailedLogins,
        eventsDispatcher,
        clock,
        config
    )

    @Test
    fun `when consecutive failed logins reach the threshold the unlocked account gets a failed-login lock`() {
        val lockedUntil = NOW.plus(LOCK_DURATION)
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.accountFor(unlocked.username) } returns unlocked
        every { consecutiveFailedLogins.increment(unlocked.username, LOCK_DURATION) } returns THRESHOLD
        every { consecutiveFailedLogins.reset(unlocked.username) } just runs
        every { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, lockedUntil) } returns true
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        uut.failedLogin(unlocked.username, CLIENT_APP_ID)

        verify { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, lockedUntil) }
        verify { consecutiveFailedLogins.reset(unlocked.username) }
        assertTrue(event.captured is FailedLoginLockAppliedEvent)
        assertEquals(unlocked.username, event.captured.userName.content)
        assertEquals(CLIENT_APP_ID, event.captured.clientAppId)
        assertEquals(NOW, event.captured.timeStamp)
        assertEquals(FailedLoginLockApplied(lockedUntil), event.captured.payload)
    }

    @Test
    fun `when consecutive failed logins are already over the threshold the account still gets a failed-login lock`() {
        val lockedUntil = NOW.plus(LOCK_DURATION)
        every { accountRepository.accountFor(unlocked.username) } returns unlocked
        every { consecutiveFailedLogins.increment(unlocked.username, LOCK_DURATION) } returns THRESHOLD + 3
        every { consecutiveFailedLogins.reset(unlocked.username) } just runs
        every { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, lockedUntil) } returns true
        every { eventsDispatcher.dispatch(any()) } just runs

        uut.failedLogin(unlocked.username, CLIENT_APP_ID)

        verify { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, lockedUntil) }
    }

    @Test
    fun `when the lock is applied its end is kept to the microsecond the database stores`() {
        val clockWithNanos = Clock.fixed(Instant.parse("2026-10-25T10:15:30.123456789Z"), ZoneOffset.UTC)
        val uut = FailedLoginLock(
            accountRepository,
            failedLoginLockStore,
            consecutiveFailedLogins,
            eventsDispatcher,
            clockWithNanos,
            FailedLoginLockConfig(threshold = THRESHOLD, duration = LOCK_DURATION)
        )
        val lockedUntil = Instant.parse("2026-10-25T10:16:30.123456Z")
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.accountFor(unlocked.username) } returns unlocked
        every { consecutiveFailedLogins.increment(unlocked.username, LOCK_DURATION) } returns THRESHOLD
        every { consecutiveFailedLogins.reset(unlocked.username) } just runs
        every { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, lockedUntil) } returns true
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        uut.failedLogin(unlocked.username, CLIENT_APP_ID)

        verify { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, lockedUntil) }
        assertEquals(FailedLoginLockApplied(lockedUntil), event.captured.payload)
    }

    @Test
    fun `when the account got locked meanwhile no failed-login lock is applied or published`() {
        every { accountRepository.accountFor(unlocked.username) } returns unlocked
        every { consecutiveFailedLogins.increment(unlocked.username, LOCK_DURATION) } returns THRESHOLD
        every { consecutiveFailedLogins.reset(unlocked.username) } just runs
        every { failedLoginLockStore.lockUntilIfUnlocked(unlocked.username, any()) } returns false

        uut.failedLogin(unlocked.username, CLIENT_APP_ID)

        verify { consecutiveFailedLogins.reset(unlocked.username) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when consecutive failed logins are below the threshold the account stays unlocked`() {
        every { accountRepository.accountFor(unlocked.username) } returns unlocked
        every { consecutiveFailedLogins.increment(unlocked.username, LOCK_DURATION) } returns THRESHOLD - 1

        uut.failedLogin(unlocked.username, CLIENT_APP_ID)

        verify(exactly = 0) { failedLoginLockStore.lockUntilIfUnlocked(any(), any()) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when an already locked account fails a login it is not counted and its lock stays lasting`() {
        val account = anAccount().lock()
        every { accountRepository.accountFor(account.username) } returns account

        uut.failedLogin(account.username, CLIENT_APP_ID)

        verify(exactly = 0) { consecutiveFailedLogins.increment(any(), any()) }
        verify(exactly = 0) { failedLoginLockStore.lockUntilIfUnlocked(any(), any()) }
    }

    @Test
    fun `when the password is proven the consecutive failed logins are reset`() {
        every { consecutiveFailedLogins.reset(anAccount().username) } just runs

        uut.passwordProven(anAccount().username)

        verify { consecutiveFailedLogins.reset(anAccount().username) }
    }

    @Test
    fun `when the threshold is zero failed logins are not counted`() {
        val disabled = failedLoginLockWith(FailedLoginLockConfig(threshold = 0, duration = LOCK_DURATION))

        disabled.failedLogin(unlocked.username, CLIENT_APP_ID)

        verify(exactly = 0) { consecutiveFailedLogins.increment(any(), any()) }
    }

    @Test
    fun `when a failed-login lock has expired the account is unlocked and the release is published`() {
        val lockedUntil = NOW.minusSeconds(1)
        val account = anAccount().lockUntil(lockedUntil)
        val event = slot<VAuthenticatorEvent>()
        every { failedLoginLockStore.releaseIfLockedUntil(account.username, lockedUntil) } returns true
        every { consecutiveFailedLogins.reset(account.username) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertEquals(account.unlock(), actual)
        verify { consecutiveFailedLogins.reset(account.username) }
        assertTrue(event.captured is FailedLoginLockReleasedEvent)
        assertEquals(account.username, event.captured.userName.content)
        assertEquals(CLIENT_APP_ID, event.captured.clientAppId)
        assertEquals(NOW, event.captured.timeStamp)
        assertEquals(FailedLoginLockRelease(FailedLoginLockReleaseReason.EXPIRED, lockedUntil), event.captured.payload)
    }

    @Test
    fun `when an expired failed-login lock was already released by another login it is not published again`() {
        val lockedUntil = NOW.minusSeconds(1)
        val account = anAccount().lockUntil(lockedUntil)
        every { failedLoginLockStore.releaseIfLockedUntil(account.username, lockedUntil) } returns false
        every { accountRepository.accountFor(account.username) } returns account.unlock()

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertTrue(actual.accountNonLocked)
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when an expired failed-login lock was made lasting meanwhile the account stays locked`() {
        val lockedUntil = NOW.minusSeconds(1)
        val account = anAccount().lockUntil(lockedUntil)
        every { failedLoginLockStore.releaseIfLockedUntil(account.username, lockedUntil) } returns false
        every { accountRepository.accountFor(account.username) } returns account.lock()

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertFalse(actual.accountNonLocked)
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when a failed-login lock ends exactly now the account is unlocked`() {
        val account = anAccount().lockUntil(NOW)
        every { failedLoginLockStore.releaseIfLockedUntil(account.username, NOW) } returns true
        every { consecutiveFailedLogins.reset(account.username) } just runs
        every { eventsDispatcher.dispatch(any()) } just runs

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertTrue(actual.accountNonLocked)
    }

    @Test
    fun `when a failed-login lock has not expired yet the account stays locked`() {
        val account = anAccount().lockUntil(NOW.plusSeconds(1))

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertEquals(account, actual)
        verify(exactly = 0) { failedLoginLockStore.releaseIfLockedUntil(any(), any()) }
    }

    @Test
    fun `when an account has a lasting lock it is never released`() {
        val account = anAccount().lock()

        val actual = uut.releaseIfExpired(account, CLIENT_APP_ID)

        assertEquals(account, actual)
        verify(exactly = 0) { failedLoginLockStore.releaseIfLockedUntil(any(), any()) }
    }

    @Test
    fun `when a username with no account fails a login nothing is counted`() {
        every { accountRepository.accountFor("unknown@domain.com") } returns null

        uut.failedLogin("unknown@domain.com", CLIENT_APP_ID)

        verify(exactly = 0) { consecutiveFailedLogins.increment(any(), any()) }
    }
}
