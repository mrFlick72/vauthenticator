---
layout: ../../layouts/DocsLayout.astro
title: Accounts & passwords
description: Support an account from signup through verification, recovery, and password lifecycle management.
section: Features · Account lifecycle
---

## What it provides

Account flows include signup, welcome email, email verification, password reset, and password change. Post-login workflows can require a password reset before the user continues.

Administrators can manage accounts and assign password lifecycle rules through the management UI or management APIs.

## Password lifecycle rules

With PostgreSQL persistence, administrators can configure recurring password reset requirements or a one-time account lock after a specified interval. Rules can be applied to a single account or in bulk using an account pattern.

The timer is counted from rule registration or its previous firing, rather than the age of the password. Changing a password does not restart the timer.

## A concrete example

A recurring reset rule can require an account to reset its password at its next sign-in after each configured interval. This capability requires the `database` profile.

[Read the password lifecycle reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/password-lifecycle.md).
