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

## Part of the local setup

The management UI runs separately from the authorization server and receives its server and client settings through runtime configuration.

[Explore local installation](../../install/local/) or [read the management UI project guide](https://github.com/mrFlick72/vauthenticator/blob/main/management-ui/AGENTS.md).
