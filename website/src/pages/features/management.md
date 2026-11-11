---
layout: ../../layouts/DocsLayout.astro
title: Management UI
description: A dedicated interface for administering your VAuthenticator installation.
section: Features · Administration
---

## What it provides

The standalone React and TypeScript management UI provides pages for client applications, accounts, roles, keys, email and SMS templates, and password lifecycle rules.

It signs in through VAuthenticator using OAuth2 with PKCE and calls the server's administration APIs.

## A concrete example

An administrator can register a client application, manage an account's roles, or update an email or SMS template through the UI. With the PostgreSQL profile, the password lifecycle page also supports account-specific and bulk rules.

## Only an administrator changes administration

An administrator is a client application with the `admin:full-access` scope, or an account holder with the `VAUTHENTICATOR_ADMIN` role signed in to the management UI. Only an administrator can give an account the `VAUTHENTICATOR_ADMIN` role or take it away, or change, reset the secret of, or delete a client application that has or would get `admin:full-access`. Other management API scopes, such as `admin:account-writer`, cannot do it: the whole request is rejected with `403`. Being signed in to the authorization server in a browser never counts as an administrator.

The same goes for managing an administrator's account: only an administrator can lock, unlock, enable or disable it, or give it password lifecycle rules. A caller with only the matching scope, such as a machine-to-machine integration, manages every other account; when a bulk password lifecycle rule matches administrator accounts, those are skipped and the response status is `207`, without saying which accounts were skipped.

The other management API scopes are not limited this way. A caller with `admin:client-app-writer` can give any of them, such as `admin:account-writer` or `admin:key-editor`, to a client application that is not an administrator, including its own, so give `admin:client-app-writer` only to client applications you trust with that much access.

The initial tenant setup is the only exception: it creates the first administrator account and client application. Administrators who forget their password recover it like any other account holder, through the reset-password link sent to their email.

## Part of the local setup

The management UI runs separately from the authorization server and receives its server and client settings through runtime configuration.

[Explore local installation](../../install/local/) or [read the management UI project guide](https://github.com/mrFlick72/vauthenticator/blob/main/management-ui/AGENTS.md).
