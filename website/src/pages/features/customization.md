---
layout: ../../layouts/DocsLayout.astro
title: Customization & infrastructure
description: Adapt token claims and select infrastructure options for your environment.
section: Features · Your environment
---

## Token customization

An optional AWS Lambda integration can enrich JWT access token and ID token claims. The Lambda receives account information and authorization context, and returns the claims to add.

For example, your integration can add application-specific claims based on the account's roles and the requesting client.

[Read the Lambda integration reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/lambda.md).

## Pages and templates

The sign-in, MFA, signup, password reset and change, email verification and error pages ship in the server as default pages, and the email and SMS templates as default templates. The tenant setup of a new installation saves both into the document store, on Amazon S3 or the filesystem, and the server serves the pages and renders the templates from there.

- **Templates:** an administrator edits the email and SMS templates from the management UI or the template APIs.
- **Pages:** an operator replaces a page by uploading their own version to the document store, at the same path.

Running the tenant setup again would put the defaults back over your changes, so it is for new installations only.

[Read the tenant setup reference](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/management.md#tenant-setup).

## Infrastructure choices

- **Persistence:** PostgreSQL.
- **Key management:** AWS KMS with the `kms` profile, or plain Java cryptography with master-key configuration.
- **Documents and assets:** filesystem or Amazon S3 storage.
- **Sessions, cache, and failed sign-in counts:** Redis. Authorizations are stored in PostgreSQL.

These options let you choose a PostgreSQL-based installation or an installation integrated with AWS services. Each option requires its corresponding configuration and dependencies.

[Read the server profile documentation](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/profiles.md).
