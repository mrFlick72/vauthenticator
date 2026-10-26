package com.vauthenticator.server.login.failedloginlock.adapter.jdbc

import com.vauthenticator.server.account.adapter.jdbc.JdbcAccountRepository
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import com.vauthenticator.server.support.JdbcUtils.jdbcTemplate
import com.vauthenticator.server.support.JdbcUtils.resetDb
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

private val LOCKED_UNTIL: Instant = Instant.parse("2026-10-25T10:16:30Z")

class JdbcFailedLoginLockRepositoryTest {

    private val accountRepository = JdbcAccountRepository(jdbcTemplate)
    private val uut = JdbcFailedLoginLockRepository(jdbcTemplate)
    private val account = anAccount()

    @BeforeEach
    fun setUp() {
        resetDb()
    }

    @Test
    fun `when an unlocked account is locked until a time the lock is stored`() {
        accountRepository.create(account.unlock())

        assertTrue(uut.lockUntilIfUnlocked(account.username, LOCKED_UNTIL))

        assertEquals(account.lockUntil(LOCKED_UNTIL), accountRepository.accountFor(account.username))
    }

    @Test
    fun `when an account that got locked meanwhile is locked until a time its lock stays lasting`() {
        accountRepository.create(account.lock())

        assertFalse(uut.lockUntilIfUnlocked(account.username, LOCKED_UNTIL))

        assertEquals(account.lock(), accountRepository.accountFor(account.username))
    }

    @Test
    fun `when a failed-login lock is released it is unlocked only once`() {
        accountRepository.create(account.lockUntil(LOCKED_UNTIL))

        assertTrue(uut.releaseIfLockedUntil(account.username, LOCKED_UNTIL))
        assertFalse(uut.releaseIfLockedUntil(account.username, LOCKED_UNTIL))

        assertEquals(account.unlock(), accountRepository.accountFor(account.username))
    }

    @Test
    fun `when a failed-login lock was made lasting meanwhile it is not released`() {
        accountRepository.create(account.lock())

        assertFalse(uut.releaseIfLockedUntil(account.username, LOCKED_UNTIL))

        assertEquals(account.lock(), accountRepository.accountFor(account.username))
    }
}
