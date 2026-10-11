---
layout: ../../layouts/DocsLayout.astro
title: OAuth2 & OpenID Connect
description: Connect your applications through standard authorization and identity protocols.
section: Features · Protocols
---

## What it provides

VAuthenticator is an OAuth2 authorization server with OpenID Connect support. It provides the authorization and token endpoints, user information, and ID tokens used by applications integrating with an identity provider.

The project also supports RP-initiated logout and OIDC Session Management.

## When it is useful

Use it when your applications need a central authorization server for sign-in and token issuance, with account and client administration alongside the protocol endpoints.

## A concrete example

The project's management UI uses the authorization code flow with PKCE to sign in through VAuthenticator, then uses an access token to call administration APIs.

[Explore the management UI](/vauthenticator/features/management/) or [review the project documentation](https://github.com/mrFlick72/vauthenticator#documentation).
