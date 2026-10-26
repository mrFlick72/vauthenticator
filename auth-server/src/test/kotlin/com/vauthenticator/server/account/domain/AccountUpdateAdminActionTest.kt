package com.vauthenticator.server.account.domain

import com.vauthenticator.server.events.EventsDispatcher
import com.vauthenticator.server.events.FailedLoginLockReleasedEvent
import com.vauthenticator.server.events.VAuthenticatorEvent
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockRelease
import com.vauthenticator.server.login.failedloginlock.FailedLoginLockReleaseReason
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
import java.time.Instant
import java.time.ZoneOffset

private val NOW: Instant = Instant.parse("2026-10-25T10:15:30Z")
private val ADMIN_CLIENT_APP_ID = ClientAppId("AN_ADMIN_CLIENT_APP_ID")

@ExtendWith(MockKExtension::class)
class AccountUpdateAdminActionTest {

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var eventsDispatcher: EventsDispatcher

    lateinit var underTest: AccountUpdateAdminAction
    val anAccount = anAccount()
    val request = AdminAccountApiRequest(
        email = anAccount.email,
        enabled = true,
        accountLocked = true,
        authorities = setOf("A_ROLE"),
        mandatoryAction = AccountMandatoryAction.NO_ACTION
    )

    @BeforeEach
    fun setUp() {
        underTest = AccountUpdateAdminAction(accountRepository, eventsDispatcher, Clock.fixed(NOW, ZoneOffset.UTC))
    }

    @Test
    fun `when we change account data`() {
        val account = anAccount.lock().copy(enabled = true, authorities = setOf("A_ROLE"))
        every { accountRepository.accountFor(account.email) } returns anAccount()
        every { accountRepository.save(account) } just runs

        underTest.execute(request, ADMIN_CLIENT_APP_ID)

        verify { accountRepository.accountFor(account.email) }
        verify { accountRepository.save(account) }
    }

    @Test
    fun `when the account is not found`() {
        val account = anAccount.lock().copy(enabled = true, authorities = setOf("A_ROLE"))
        every { accountRepository.accountFor(account.email) } returns null

        underTest.execute(request, ADMIN_CLIENT_APP_ID)

        verify { accountRepository.accountFor(account.email) }
        verify(exactly = 0) { accountRepository.save(account) }
    }

    @Test
    fun `when an account under a failed-login lock is saved still locked the lock stays temporary`() {
        val locked = anAccount.lockUntil(NOW.plusSeconds(30))
        every { accountRepository.accountFor(locked.email) } returns locked
        every { accountRepository.save(any()) } just runs

        underTest.execute(request, ADMIN_CLIENT_APP_ID)

        verify { accountRepository.save(locked.copy(enabled = true, authorities = setOf("A_ROLE"))) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }

    @Test
    fun `when an account under a failed-login lock is unlocked the lock ends and the release is published`() {
        val lockedUntil = NOW.plusSeconds(30)
        val locked = anAccount.lockUntil(lockedUntil)
        val event = slot<VAuthenticatorEvent>()
        every { accountRepository.accountFor(locked.email) } returns locked
        every { accountRepository.save(any()) } just runs
        every { eventsDispatcher.dispatch(capture(event)) } just runs

        underTest.execute(request.copy(accountLocked = false), ADMIN_CLIENT_APP_ID)

        verify {
            accountRepository.save(
                locked.unlock().copy(enabled = true, authorities = setOf("A_ROLE"))
            )
        }
        assertTrue(event.captured is FailedLoginLockReleasedEvent)
        assertEquals(locked.username, event.captured.userName.content)
        assertEquals(ADMIN_CLIENT_APP_ID, event.captured.clientAppId)
        assertEquals(NOW, event.captured.timeStamp)
        assertEquals(
            FailedLoginLockRelease(FailedLoginLockReleaseReason.ADMIN_UNLOCK, lockedUntil),
            event.captured.payload
        )
    }

    @Test
    fun `when an account with a lasting lock is unlocked no failed-login lock release is published`() {
        val locked = anAccount.lock()
        every { accountRepository.accountFor(locked.email) } returns locked
        every { accountRepository.save(any()) } just runs

        underTest.execute(request.copy(accountLocked = false), ADMIN_CLIENT_APP_ID)

        verify { accountRepository.save(locked.unlock().copy(enabled = true, authorities = setOf("A_ROLE"))) }
        verify(exactly = 0) { eventsDispatcher.dispatch(any()) }
    }
}
