package com.vauthenticator.server.password.domain.lifecycle

import com.vauthenticator.server.account.domain.Account
import com.vauthenticator.server.support.AccountTestFixture.anAccount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class AccountLockPasswordLifeCycleStrategyImplementationTest : AbstractPasswordLifeCycleStrategyTest() {

    override fun initImplementation(): PasswordLifeCycleStrategyImplementation =
        AccountLockPasswordLifeCycleStrategyImplementation()

    override fun passwordLifeCycleRuleAction(): PasswordLifeCycleAction =
        PasswordLifeCycleAction.ACCOUNT_LOCK

    override fun accountWithoutActionInEffect(): Account =
        anAccount().unlock()

    override fun accountWithActionApplied(account: Account): Account =
        account.lock()

    @Test
    fun `when the account is under a failed-login lock the lock becomes lasting`() {
        val locked = anAccount().lockUntil(Instant.parse("2026-12-20T10:16:30Z"))

        val actual = AccountLockPasswordLifeCycleStrategyImplementation().apply(locked)

        assertEquals(locked.lock(), actual)
    }

    @Test
    fun `when the account already has a lasting lock nothing changes`() {
        val locked = anAccount().lock()

        assertNull(AccountLockPasswordLifeCycleStrategyImplementation().apply(locked))
    }
}
