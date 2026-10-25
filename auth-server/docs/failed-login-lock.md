# Failed-Login Lock

## Abstract

When an account's **Consecutive Failed Logins** reach a configured threshold, VAuthenticator locks the account with a
**Failed-Login Lock**: an account lock that ends on its own once the configured duration has passed. The domain
vocabulary is defined in [CONTEXT.md](../CONTEXT.md), and the design decision in
[ADR 0011](adr/0011-failed-login-lock-reuses-account-lock.md).

The feature works with the `database` profile (PostgreSQL). With the `dynamo` profile, which is being removed, the lock
end is not stored, so a Failed-Login Lock would be lasting.

### What counts

- Only wrong passwords on the login form count. A wrong MFA code or a failed password change does not.
- The count is per account (username) and consecutive: a successful login resets it, and so does a period of one lock
  duration without a failure.
- Usernames with no account count for nothing.
- While the account is locked every login is refused, even with the right password, with the same generic login error
  as any failed login. Those attempts are not counted and do not extend the lock.

### How the lock ends

The lock reuses the account's locked flag, marked as temporary by a lock end (`lockedUntil`) stored on the account.
There is no scheduler: at the first login attempt after the lock end, the account is unlocked before the password is
checked. Only an account that is unlocked gets a Failed-Login Lock, so locks set by signup (email verification), by an
admin, or by the `ACCOUNT_LOCK` password lifecycle action never expire on their own.

- **Admin save** (`PUT /api/admin/accounts`): only a change of `accountLocked` is an explicit lock or unlock. Saving
  `accountLocked: true` for an account under a Failed-Login Lock keeps it temporary; saving `false` ends it at once.
- **Password lifecycle**: the `ACCOUNT_LOCK` action firing on an account under a Failed-Login Lock makes the lock
  lasting.
- **Not affected**: sessions and tokens issued before the lock keep working; password reset neither lifts the lock nor
  is blocked by it.

`GET /api/admin/accounts/{email}/email` returns `lockedUntil` (an ISO-8601 instant, or `null` for a lasting lock or no
lock).

### Events

- `FailedLoginLockAppliedEvent`: payload (`FailedLoginLockApplied`) is the lock end. Published when the lock is
  applied.
- `FailedLoginLockReleasedEvent`: payload (`FailedLoginLockRelease`) is the reason (`EXPIRED` or `ADMIN_UNLOCK`) and
  the lock end. Published when the lock actually ends. An expired lock is released at the next login attempt, so the event time can be later
  than the lock end, and no event is published if nobody tries to log in again.

A login with no client app (a direct `/login`) attributes its events to the system client.

## Configuration

```yaml
login:
  failed-login-lock:
    threshold: 5    # consecutive failed logins that lock the account; 0 turns the feature off
    duration: 1m    # how long the lock lasts, and how long an idle failure count is kept
```

Both have the defaults shown, so the feature is on without any configuration. With a threshold of `0` no new locks
are applied, but existing ones still expire.

The failure count is kept in Redis under the key `consecutive_failed_logins_<username>`.

## Upgrading

The `ACCOUNT` table has a new nullable column. There is no migration tool, so existing PostgreSQL databases need:

```sql
ALTER TABLE ACCOUNT ADD COLUMN locked_until TIMESTAMP WITH TIME ZONE;
```
