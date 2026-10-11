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

## Infrastructure choices

- **Persistence:** PostgreSQL with the `database` profile, or DynamoDB with `dynamo`.
- **Key management:** AWS KMS with the `kms` profile, or plain Java cryptography with master-key configuration.
- **Documents and assets:** filesystem or Amazon S3 storage.
- **Sessions and cache:** Redis.

These options let you choose a PostgreSQL-based installation or an installation integrated with AWS services. Each option requires its corresponding configuration and dependencies.

[Read the server profile documentation](https://github.com/mrFlick72/vauthenticator/blob/main/auth-server/docs/profiles.md).
