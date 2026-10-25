# A Failed-Login Lock reuses the account lock, marked temporary by a lock end time

For issue [#211](https://github.com/mrFlick72/vauthenticator/issues/211) an account whose **Consecutive Failed Logins** reach a configured threshold is locked for a configured duration (defaults: 5 failures, `1m`; a threshold of `0` turns it off). The lock reuses the account's existing locked flag instead of a separate lockout concept, to keep one notion of "this account cannot log in". The flag already carries lasting locks (signup waiting for email verification, an admin lock, the lock **Lifecycle Action**), so the temporary lock is told apart by a lock end time stored on the account next to the flag: only a lock with an end time ever expires.

- **Only an unlocked account gets a Failed-Login Lock.** Failed logins against an already-locked account never set an end time, so they can never turn a lasting lock into one that expires.
- **Expiry is lazy.** At the next login attempt after the end time, the account is unlocked and the end time cleared before the password is checked. There is no scheduler, so the deferred cluster-safe jobs problem does not apply.
- **An explicit unlock ends it; an explicit lock of an unlocked account is lasting.** An admin save that re-states "locked" for an account under a Failed-Login Lock (the admin API always sends the whole account) leaves it temporary; otherwise saving an unrelated change would lock the account forever. The lock **Lifecycle Action** is a real lock, not a re-statement: firing on an account under a Failed-Login Lock makes the lock lasting, because the one-shot rule is spent either way and would otherwise be lost when the temporary lock expires.
- **While locked, every login is refused**, even with the right password, with the same generic error as any failed login. Those attempts neither count nor extend the lock, so an attacker cannot keep an account locked indefinitely by retrying during the lock.
- **Storage is split by what losing it costs.** The failure count lives in Redis, keyed by username, with a TTL of one lock duration renewed on each failure (an idle count fades out); losing it only gives an attacker a few extra attempts. The end time lives on the account (PostgreSQL only: DynamoDB support is being removed, and there the field is not stored, so a Failed-Login Lock would be lasting), because losing it while the account is locked would leave the account locked forever.

**Considered options:**
- A separate temporary-lockout concept that never touches the locked flag: rejected for simplicity, at the cost of the guard rules above.
- A scheduled job that unlocks expired accounts: rejected. It needs cluster-safe scheduling and only unlocks as precisely as its polling interval.
- The end time in Redis with a TTL: rejected. Redis eviction or restart would leave the flag set with nothing marking it temporary.

**Consequences:**
- PostgreSQL gets a nullable `locked_until` column in `schema.sql`. There is no migration tool, so existing databases need a manual `ALTER TABLE` documented in the upgrade notes.
- The admin API returns a read-only `lockedUntil` (null for a lasting lock or no lock). Showing it in management-ui is a follow-up.
- `FailedLoginLockAppliedEvent` (username, client app, `lockedUntil`) is published when the lock is applied, and `FailedLoginLockReleasedEvent` (reason `EXPIRED` or `ADMIN_UNLOCK`, `lockedUntil`) when it actually ends. Because expiry is lazy, an expired lock's release event fires at the next login attempt, not at `lockedUntil`, and never fires if nobody tries to log in again.
- Sessions and tokens issued before the lock keep working, and password reset neither lifts the lock nor is blocked by it.
- An admin cannot make a Failed-Login Lock lasting in one save; they unlock and lock again.
