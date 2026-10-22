---
layout: ../../layouts/DocsLayout.astro
title: Multi-factor authentication
description: Add another verification step to the sign-in experience.
section: Features · Authentication
---

## What it provides

VAuthenticator includes MFA support with email, SMS, and OTP methods. The login workflow integrates MFA challenges, while enrollment APIs support registering and associating devices.

An MFA device is one method (email or SMS) on one channel: an email address or a phone number. A device is enrolled when it is registered, and associated once the account holder confirms the code sent to it. Only associated devices count as a second factor. One associated device is the default device, which receives the login challenge unless the sign-in page picks another one.

## When it is useful

Use MFA when a password alone is insufficient for the sign-in experience your application needs. A client application requires it with the `mfa:always` scope. The available channel depends on how the server and its communication services are configured.

## How MFA applies at sign-in

A client application with the `mfa:always` scope gets a sign-in only once the account holder has passed the MFA challenge. The check runs every time an application asks for a sign-in, so leaving the MFA page does not skip it. A challenge passed once counts for every application that requires MFA for the rest of the session, and signing in again with the password starts over.

A client application with `mfa:always` must also have the `mfa:enrollment` scope. The MFA sign-in page lists the account's devices on behalf of that application, so without `mfa:enrollment` it cannot find a device and every code is rejected as wrong or expired.

## Managing MFA devices

Your application can let account holders manage their own devices through the API, with a bearer token carrying the `mfa:enrollment` scope:

- enroll a new email address or phone number, and associate it with the code it receives
- list the account's devices, with the email addresses and phone numbers masked
- choose the default device
- revoke a device they no longer use

Revocation always leaves the account with a usable second factor. The account's last associated device cannot be revoked, and the default device can be revoked only after another device has been made the default. A revoked device and its key are deleted; enrolling the same channel again creates a new device.

The login check fails closed. If the device cannot be found, belongs to another account, or the account has no default device, the second factor is rejected rather than skipped.

## A concrete example

For an email channel, an account enrolls the destination, receives a verification code, and confirms the association. During login, a challenge is sent to the enrolled channel for verification.

To replace that email with a phone, the account enrolls and associates the phone, makes it the default device, and then revokes the email device. Trying to revoke the email while it is still the default is refused with `409 Conflict` and the reason `DEFAULT_DEVICE`.

[Read the MFA API reference, including a revocation walkthrough](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/mfa.md).

[See the sequence diagrams of each MFA use case](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/mfa-sequence-diagrams.md).

[Read how the login workflow runs MFA and the mandatory password change](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/login-workflow.md).
