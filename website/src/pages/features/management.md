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

The initial tenant setup is the only exception: it creates the first administrator account and client application. Administrators who forget their password recover it like any other account holder, through the reset-password link sent to their email.

## Part of the local setup

The management UI runs separately from the authorization server and receives its server and client settings through runtime configuration.

[Explore local installation](../../install/local/) or [read the management UI project guide](https://github.com/mrFlick72/vauthenticator/blob/main/management-ui/AGENTS.md).
