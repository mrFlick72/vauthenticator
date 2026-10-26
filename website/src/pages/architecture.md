---
layout: ../layouts/DocsLayout.astro
title: Architecture
description: The authorization server, management UI, and Helm charts form the VAuthenticator ecosystem.
section: Architecture
---

## Authorization server

The Kotlin and Spring Boot application provides OAuth2 and OpenID Connect endpoints, sign-in pages, account lifecycle flows, MFA, and administration APIs. Your applications connect to this server for authentication and authorization.

## Management UI

A separate React and TypeScript application lets administrators manage clients, accounts, roles, keys, email and SMS templates, and password lifecycle rules. It authenticates through the authorization server and calls its management APIs.

## Infrastructure

Persistence can use PostgreSQL or DynamoDB. Redis supports distributed sessions, caching, and failed sign-in counts, and stores authorizations when PostgreSQL is not used. Key management can use plain Java cryptography or AWS KMS. Documents and static assets can be loaded from the filesystem or Amazon S3.

## Deployment

The repository contains separate Helm charts for the server and management UI. Begin with the [local installation overview](../install/local/), then explore the [Kubernetes guide](../install/kubernetes/).

[View the source repository and architecture documentation](https://github.com/mrFlick72/vauthenticator).
