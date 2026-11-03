---
layout: ../../layouts/DocsLayout.astro
title: Accounts & passwords
description: Support an account from signup through verification, recovery, failed-login protection, a sign-in audit trail, and password lifecycle management.
section: Features · Account lifecycle
---

## What it provides

Account flows include signup, welcome email, email verification, password reset, and password change. A pending password change must be completed before any application accepts the sign-in. It is presented right after the password, after MFA when the application requires it.

The links these flows send work for a limited time: an email-verification link for 24 hours and a reset-password link for 15 minutes by default. Both are server settings, also exposed by the Helm chart. Verification links sent before upgrading to 0.10 stop working; the account holder asks for a new verification email.

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

Existing PostgreSQL databases need a new column before upgrading; the reference below gives the statement.

[Read the failed-login lock reference, with sequence diagrams of each flow](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/failed-login-lock.md).

## Sign-in audit trail

The server can write an entry to its log for every sign-in step and account event: each password check and MFA check, passed or failed, each sign-out, and signup, password change and reset, failed-login locks and their release, password lifecycle actions and MFA device revocation. It is off by default; turn it on with a server setting, also exposed by the Helm chart as `application.events.enableLoggerConsumer`:

```yaml
event:
  consumer:
    enable:
      logger-event-consumer: true
```

- Each entry names the account, the application the sign-in was for (`none` for a direct sign-in), and where the request came from: IP address, browser user agent, and a session reference that groups the entries of a signed-in session without revealing the session itself (it changes once the password is proven, so earlier attempts carry a different one).
- Behind a proxy or ingress the IP address is read from `X-Forwarded-For`, so it is only as reliable as the proxy that sets that header.
- Passwords, even encoded, and session identifiers are never written. Text a visitor controls, such as a typed username or the user agent, is escaped so it cannot add fake entries to the log.
- IP addresses and user agents are personal data: whoever turns the trail on decides how long the logs are kept.

[Read the events reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/events.md).

## Password lifecycle rules

With PostgreSQL persistence, administrators can configure recurring password reset requirements or a one-time account lock after a specified interval. Rules can be applied to a single account or in bulk using an account pattern.

The timer is counted from rule registration or its previous firing, rather than the age of the password. Changing a password does not restart the timer.

## A concrete example

A recurring reset rule can require an account to reset its password at its next sign-in after each configured interval.

[Read the password lifecycle reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/password-lifecycle.md).
