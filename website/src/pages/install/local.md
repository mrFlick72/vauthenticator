---
layout: ../../layouts/DocsLayout.astro
title: Local installation
description: Explore VAuthenticator with a running authorization server, management UI, and local tenant.
section: Get started · Local environment
---

## What you will set up

The local environment includes the authorization server, management UI, supporting infrastructure, and a provisioned local tenant. The existing setup also covers email verification and MFA before signing in as an administrator.

## Setup path

The local setup uses Docker Compose for infrastructure, a separately started authorization server, and a management UI container built from source.

Use the existing guides for the required toolchain, hostnames, server configuration, frontend assets, and tenant provisioning:

1. [Project local development overview](https://github.com/mrFlick72/vauthenticator#local-development).
2. [Local infrastructure and tenant setup](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/local/readme.md).
3. [Authorization server development guide](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/AGENTS.md#local-development).
4. [Management UI local configuration](https://github.com/mrFlick72/vauthenticator/blob/main/management-ui/AGENTS.md#environment-and-local-runtime).

For Kubernetes packaging, see the [Helm installation overview](/vauthenticator/install/kubernetes/).
