package com.vauthenticator.server.login.failedloginlock.adapter.jdbc

import com.vauthenticator.server.login.failedloginlock.FailedLoginLockStore
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Timestamp
import java.time.Instant

private const val LOCK_UNTIL_IF_UNLOCKED_QUERY = """
    UPDATE ACCOUNT SET account_non_locked = false, locked_until = ?
     WHERE username = ? AND account_non_locked = true
    """

private const val RELEASE_IF_LOCKED_UNTIL_QUERY = """
    UPDATE ACCOUNT SET account_non_locked = true, locked_until = NULL
     WHERE username = ? AND account_non_locked = false AND locked_until = ?
    """

class JdbcFailedLoginLockStore(private val jdbcTemplate: JdbcTemplate) : FailedLoginLockStore {

    override fun lockUntilIfUnlocked(userName: String, lockedUntil: Instant): Boolean =
        jdbcTemplate.update(LOCK_UNTIL_IF_UNLOCKED_QUERY, Timestamp.from(lockedUntil), userName) == 1

    override fun releaseIfLockedUntil(userName: String, lockedUntil: Instant): Boolean =
        jdbcTemplate.update(RELEASE_IF_LOCKED_UNTIL_QUERY, userName, Timestamp.from(lockedUntil)) == 1
}
