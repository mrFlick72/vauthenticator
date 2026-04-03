# VAuthenticator Agent Guide

## Purpose

This repository contains the VAuthenticator authorization server. It is a Spring Boot 4 application written in Kotlin that provides OAuth2 Authorization Server and OpenID Connect capabilities, plus account lifecycle, MFA, password flows, role/group management, communication templates, and a small React/TypeScript frontend asset bundle for user-facing pages.

The main application entry point is [src/main/kotlin/com/vauthenticator/server/VAuthenticatorApplication.kt](src/main/kotlin/com/vauthenticator/server/VAuthenticatorApplication.kt).

## Stack

- Backend: Spring Boot 4.0.x, Kotlin 2.2, Java 21
- Security: Spring Security, Spring Authorization Server, OAuth2 Resource Server
- Persistence: PostgreSQL or DynamoDB depending on active profile
- Cache/session: Redis, Spring Session
- Key management: AWS KMS or plain Java crypto depending on profile
- Templating: Thymeleaf for pages, Jinjava for communication templates
- Frontend assets: React 19, TypeScript, webpack, MUI
- Testing: JUnit 5, Spring Boot Test, MockK, GreenMail, Jacoco

## Repository Layout

- `src/main/kotlin/com/vauthenticator/server`: backend source
- `src/test/kotlin/com/vauthenticator/server`: tests
- `src/main/resources`: Spring config, SQL schema, i18n messages, HTML templates
- `src/main/frontend`: React/TypeScript asset source and webpack config
- `communication/default/email`: default email templates used for communication rendering and local asset/template build flows
- `docs`: focused feature docs for profiles, management endpoints, MFA, and lambda integration
- `local`: local development compose files, sample config, helper script, and HTTP requests
- `iac/terraform`: infrastructure-as-code assets

## Backend Module Map

The backend is organized by domain-oriented packages. The important ones are:

- `account`: account CRUD, signup, welcome/verification flows, account repositories and web controllers
- `communication`: email/SMS abstractions and template resolution
- `document`: document/template loading from filesystem or S3
- `keys`: signing/encryption key generation, storage, and rotation
- `login`: login page, Spring Security user details, and login workflow engine
- `management`: actuator-style setup and cleanup endpoints, including tenant bootstrap
- `mfa`: MFA enrollment, challenge, association, and login workflow integration
- `oauth2`: authorization storage, registered client adaptation, and token enhancement
- `oidc`: logout, session management, userinfo, and ID token enrichment
- `password`: password reset, change, generation, and policy/history logic
- `role`: groups, roles, and permission validation
- `ticket`: ticket creation and storage used by multi-step flows
- `web`: shared MVC/web helpers, error pages, static assets, CORS configuration
- `config`: Spring configuration for auth server, database, AWS, security, and time

The package structure generally follows `api`, `domain`, `adapter`, and `web` subpackages where applicable.

## Frontend

The frontend is not a standalone SPA. It builds page-specific bundles that are served by the backend.

Important locations:

- `src/main/frontend/app/login`
- `src/main/frontend/app/mfa`
- `src/main/frontend/app/signup`
- `src/main/frontend/app/reset-password`
- `src/main/frontend/app/change-password`
- `src/main/frontend/app/errors`
- `src/main/frontend/app/component`

Webpack entry points are defined in `src/main/frontend/webpack.config.js`. The local helper can build the frontend bundles and copy them into the filesystem-based asset/template layout used for local development.

## Runtime Profiles

Profile behavior is documented in `docs/profiles.md`.

- `database`: use PostgreSQL persistence
- `dynamo`: use DynamoDB persistence
- `kms`: use AWS KMS for key management
- omit `kms`: use plain Java crypto-based key management

When KMS is not enabled, the application expects a master-key storage configuration in Spring properties.

## Local Development

The local environment depends on multiple services: PostgreSQL, DynamoDB/LocalStack, KMS emulation, Redis, and an email server. See `local/readme.md` and `local/docker-compose.yml`.

Local hostnames expected by the project:

- `local.api.vauthenticator.com`
- `local.management.vauthenticator.com`

Useful local endpoint:

- `POST /actuator/tenant-setup` provisions a default local tenant

The local docs also describe default admin/client credentials and the frontend/document filesystem mode:

```yaml
document:
  engine: file-system
  fs-base-path: dist
```

## Build And Test Commands

Backend:

- `./mvnw test`
- `./mvnw package`
- `./mvnw spring-boot:run`

Frontend:

- `cd src/main/frontend && npm install`
- `cd src/main/frontend && npm run build`
- `cd src/main/frontend && npm run production-build`
- `cd src/main/frontend && npm run watch`

Combined local asset/template build:

- `bash local/build-frontend.sh`

Note: `local/build-frontend.sh` rebuilds frontend assets and copies email templates into the local filesystem-based document/template layout.

## Management And Feature Endpoints

Documented custom management endpoints:

- `POST /actuator/database-clean-up`
- `POST /actuator/tenant-setup`

Documented MFA endpoints:

- `POST /api/mfa/enrollment`
- `POST /api/mfa/associate`

Lambda-based token customization is supported when `vauthenticator.lambda.aws.enabled=true`. The documented default lambda name is `vauthenticator-token-enhancer`.

## Conventions For Changes

- Keep changes within the existing domain package boundaries. If a feature belongs to account, MFA, password, or OAuth concerns, extend that domain rather than adding generic helpers at the root.
- Prefer existing package patterns: `api` for HTTP endpoints, `domain` for business logic and contracts, `adapter` for integrations, `web` for MVC/page concerns.
- Check both backend and frontend paths when changing user flows like login, signup, password reset, MFA, or error pages.
- If a change affects templates or frontend bundles, verify whether frontend assets or communication templates must be rebuilt or copied for the local filesystem-based setup.
- Be careful with profile-specific behavior. Repository, key, and infrastructure code may have both AWS-backed and local/database-backed implementations.
- Tests are organized near the same domain names in `src/test/kotlin/com/vauthenticator/server`.

## Files Worth Reading First

- `pom.xml`
- `local/readme.md`
- `docs/profiles.md`
- `docs/management.md`
- `docs/mfa.md`
- `docs/lambda.md`
- `src/main/kotlin/com/vauthenticator/server/config/AuthorizationServerConfig.kt`
- `src/main/kotlin/com/vauthenticator/server/config/WebSecurityConfig.kt`
- `src/main/frontend/webpack.config.js`

## Practical Notes For Future Agents

- The git worktree was clean when this guide was created.
- `application.yml` at the root resource level is intentionally minimal, so expect most behavior to come from profile-specific or component-specific configuration classes and local overrides.
- The local helper scripts and docs are important in this project because a large part of the runtime setup lives outside the default Spring Boot process.
