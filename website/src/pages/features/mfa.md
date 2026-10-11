---
layout: ../../layouts/DocsLayout.astro
title: Multi-factor authentication
description: Add another verification step to the sign-in experience.
section: Features · Authentication
---

## What it provides

VAuthenticator includes MFA support with email, SMS, and OTP methods. The login workflow integrates MFA challenges, while enrollment APIs support registering and associating devices.

## When it is useful

Use MFA when a password alone is insufficient for the sign-in experience your application needs. The available channel depends on how the server and its communication services are configured.

## A concrete example

For an email channel, an account enrolls the destination, receives a verification code, and confirms the association. During login, a challenge is sent to the enrolled channel for verification.

[Read the MFA enrollment and challenge API reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/mfa.md).
