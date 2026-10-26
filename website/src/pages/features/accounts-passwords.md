---
layout: ../../layouts/DocsLayout.astro
title: Accounts & passwords
description: Support an account from signup through verification, recovery, failed-login protection, and password lifecycle management.
section: Features · Account lifecycle
---

## What it provides

Account flows include signup, welcome email, email verification, password reset, and password change. A pending password change must be completed before any application accepts the sign-in. It is presented right after the password, after MFA when the application requires it.

Administrators can manage accounts and assign password lifecycle rules through the management UI or management APIs.

## Failed-login lock

After too many consecutive wrong passwords on the sign-in page, the account is locked for a while and then unlocks on its own. By default five wrong passwords in a row lock the account for one minute; both values are server settings, and a threshold of `0` turns the protection off:

```yaml
login:
  failed-login-lock:
    threshold: 5
    duration: 1m
```

- Only wrong passwords count. A wrong MFA code or a failed password change does not, and a correct password starts the count again.
- While the lock lasts, every sign-in is refused, even with the right password, with the same error as any failed sign-in, so the page does not reveal whether an account exists or is locked. Those attempts do not extend the lock.
- The lock never replaces a lock an administrator set: only an unlocked account gets one, and an administrator can unlock it at any time from the management UI or API.
- Sessions and tokens issued before the lock keep working, and password reset is not affected.

This protection requires the `database` profile. Existing PostgreSQL databases need a new column before upgrading; the reference below gives the statement.

[Read the failed-login lock reference, with sequence diagrams of each flow](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/failed-login-lock.md).

## Password lifecycle rules

With PostgreSQL persistence, administrators can configure recurring password reset requirements or a one-time account lock after a specified interval. Rules can be applied to a single account or in bulk using an account pattern.

The timer is counted from rule registration or its previous firing, rather than the age of the password. Changing a password does not restart the timer.

## A concrete example

A recurring reset rule can require an account to reset its password at its next sign-in after each configured interval. This capability requires the `database` profile.

[Read the password lifecycle reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/password-lifecycle.md).
