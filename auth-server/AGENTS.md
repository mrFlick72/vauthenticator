# VAuthenticator Agent Guide

## Purpose

This repository contains the VAuthenticator authorization server. It is a Spring Boot 4 application written in Kotlin that provides OAuth2 Authorization Server and OpenID Connect capabilities, plus account lifecycle, MFA, password flows, role/group management, communication templates, and a small React/TypeScript frontend asset bundle for user-facing pages.

The main application entry point is [src/main/kotlin/com/vauthenticator/server/VAuthenticatorApplication.kt](src/main/kotlin/com/vauthenticator/server/VAuthenticatorApplication.kt).

Use this guide for any work under `auth-server`. Per the repo root instructions, this file takes precedence over the monorepo-level `AGENTS.md` for this subtree.

The relevant skills if available, for working in this repository include: $kotlin-patterns $kotlin-specialist $kotlin-springboot  $kotlin-testing

As additional capability I would suggest to use the context7 MCP server to gain more details if needed

## Stack

- Backend: Spring Boot 4.0.x, Kotlin 2.3, Java 25
- Security: Spring Security, Spring Authorization Server, OAuth2 Resource Server
- Persistence: PostgreSQL (DynamoDB was removed in 0.10, ADR 0012)
- Cache/session: Redis, Spring Session
- Key management: AWS KMS or plain Java crypto depending on profile
- Templating: Thymeleaf for pages, Jinjava for communication templates
- Frontend assets: React 19, TypeScript, webpack, MUI
- Testing: JUnit 5, Spring Boot Test, MockK, GreenMail, Jacoco

## Architecture decisions and conventions

- *Prefer* ?: throw pattern over the requireNotNull. or checkNotNull
- *Prefer* Custom exceptions over generic ones, even if they are unchecked. This helps with error handling and makes it easier to understand the context of the error when it is thrown.
- *Prefer* domain-specific packages and classes over generic ones. This helps to keep the code organized and makes it easier to find the relevant code when working on a specific feature or bug.

## Repository Layout

- `src/main/kotlin/com/vauthenticator/server`: backend source
- `src/test/kotlin/com/vauthenticator/server`: tests
- `src/main/resources`: Spring config, SQL schema, i18n messages, HTML templates
- `src/main/frontend`: React/TypeScript asset source and webpack config
- `communication/default/email`: default email templates used for communication rendering and local asset/template build flows
- `communication/default/sms`: default SMS templates (today the MFA challenge text), same flows as the email templates
- `docs`: focused feature docs for profiles, management endpoints, MFA, the login workflow, password lifecycle, the Failed-Login Lock, and lambda integration
- `local`: local development compose files, sample config, helper script, and HTTP requests
- `iac/terraform`: infrastructure-as-code assets

## Backend Module Map

The backend is organized by domain-oriented packages. The important ones are:

- `account`: account CRUD, signup, welcome/verification flows, account repositories and web controllers
- `communication`: email/SMS abstractions and template resolution
- `document`: document/template loading from filesystem or S3
- `keys`: signing/encryption key generation, storage, and rotation
- `login`: login page, Spring Security user details, the login workflow (`login/workflow`, see `docs/login-workflow.md`), and the Failed-Login Lock (`login/failedloginlock`, see `docs/failed-login-lock.md`)
- `management`: actuator-style setup and cleanup endpoints, including tenant bootstrap
- `mfa`: MFA enrollment, challenge, association, and login workflow integration
- `oauth2`: authorization storage, registered client adaptation, and token enhancement
- `oidc`: RP-initiated logout, OIDC Session Management, userinfo, and ID token enrichment
- `password`: password reset, change, generation, policy/history logic, and password lifecycle rules (`domain/lifecycle`) evaluated by a scheduled job
- `role`: groups, roles, and permission validation
- `ticket`: ticket creation and storage used by multi-step flows; a ticket expires on read after a lifetime per purpose (`ticket.ttl.*`, ADR 0014)
- `web`: shared MVC/web helpers, error pages, static assets, CORS configuration
- `config`: Spring configuration for auth server, AWS, security, and time; each domain's beans live in a config class in its own package (for example `AccountConfig`, `RoleRepositoryConfig`)

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

- `kms`: use AWS KMS for key management
- omit `kms`: use plain Java crypto-based key management

When KMS is not enabled, the application expects a master-key storage configuration in Spring properties.

## Local Development

The local environment depends on multiple services: PostgreSQL, LocalStack (KMS and SNS emulation), Redis, and an email server. See `local/readme.md` and `local/docker-compose.yml`.

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

Use a Java 25 runtime/toolchain for Maven builds and local runs. The application image is based on `amazoncorretto:25-al2023`.

Frontend:

- `cd src/main/frontend && npm install`
- `cd src/main/frontend && npm run build`
- `cd src/main/frontend && npm run production-build`
- `cd src/main/frontend && npm run watch`

Combined local asset/template build:

- `bash local/build-frontend.sh`

Note: `local/build-frontend.sh` rebuilds frontend assets and copies email and SMS templates into the local filesystem-based document/template layout.

## Management And Feature Endpoints

Documented custom management endpoints:

- `POST /actuator/database-clean-up`
- `POST /actuator/tenant-setup`

Documented MFA endpoints (see `docs/mfa.md`):

- `POST /api/mfa/enrollment`
- `POST /api/mfa/associate` (failures carry a `reason`: `WRONG_CODE`, `INVALID_TICKET`, `ALREADY_ASSOCIATED`; redeems only `mfaEnrollment` tickets of the caller, at most five codes checked per ticket, counted in `TICKET.code_attempts` before the check, which needs a manual `ALTER TABLE` on existing databases; ADR 0013)
- `GET /api/mfa/enrollment` (`associated=true|false|all`, default `true`: associated devices only; also used by the login MFA page via the session; ADR 0015)
- `PUT /api/mfa/device`
- `DELETE /api/mfa/enrollment/{mfaDeviceId}` (revoke a device; rules in ADR 0009)
- `PUT /api/mfa/challenge` (also used by the login MFA page via the session)
- `GET /api/sms-template/{smsType}` / `PUT /api/sms-template` (MFA SMS text, scopes `admin:sms-template-reader` / `admin:sms-template-writer`; mirrors `/api/email-template`)

Documented password lifecycle endpoints (scope `admin:password-lifecycle-editor`, see `docs/password-lifecycle.md`):

- `PUT /api/admin/accounts/{userName}/password/lifecycle/{action}`
- `GET /api/admin/accounts/{userName}/password/lifecycle`
- `DELETE /api/admin/accounts/{userName}/password/lifecycle/{action}`
- `POST /api/admin/accounts/password/lifecycle/{action}/bulk` (account pattern, e.g. `*@gmail.com`)
- `DELETE /api/admin/accounts/password/lifecycle/{action}/bulk?accountPattern=...`

The password lifecycle domain vocabulary is defined in `CONTEXT.md`.

Lambda-based token customization is supported when `vauthenticator.lambda.aws.enabled=true`. The documented default lambda name is `vauthenticator-token-enhancer`.

## Conventions For Changes

- Keep changes within the existing domain package boundaries. If a feature belongs to account, MFA, password, or OAuth concerns, extend that domain rather than adding generic helpers at the root.
- Prefer existing package patterns: `api` for HTTP endpoints, `domain` for business logic and contracts, `adapter` for integrations, `web` for MVC/page concerns.
- Check both backend and frontend paths when changing user flows like login, signup, password reset, MFA, or error pages.
- If a change affects templates or frontend bundles, verify whether frontend assets or communication templates must be rebuilt or copied for the local filesystem-based setup.
- Be careful with profile-specific behavior. Key management has both an AWS KMS and a plain-Java implementation; persistence is PostgreSQL only.
- The account, role and client-application Redis caches are opt-in (`vauthenticator.cache.<region>.enabled`) and wired by the `*RepositoryConfig` classes. Any code that writes account tables without going through `CachedAccountRepository` must evict the account cache (a decorator such as `AccountCacheEvictingFailedLoginLockRepository`) and have a test that a cached account reflects the write. See ADR 0012.
- Tests are organized near the same domain names in `src/test/kotlin/com/vauthenticator/server`.
- `/api/**` authorization lives in the controllers, not in `WebSecurityConfig`. A JWT-only handler binds `Authentication?` and calls `PermissionValidator.validate(authentication, scopes)` (or `jwtAuthenticationOf(authentication)` when there is no scope to check), which returns the validated `JwtAuthenticationToken`. Never bind `JwtAuthenticationToken` directly: `/api/**` `.authenticated()` also accepts browser sessions, so a session caller would get a `500` instead of a `401`. Cover each JWT-only handler with a standalone controller test for "session user, even an admin → `401`". See ADR 0007. Session-aware handlers (browser flows) call `validate(authentication, session, scopes)`; a request without a resolvable session client app gets `403`. Ticket-based flows authorize by the ticket: set a purpose in the ticket context when issuing it and check it when redeeming (ADR 0008).
- Architecture decisions are recorded in `docs/adr/`. Read the related ADRs before changing authorization, and add a new one when a decision changes.
- The login workflow (MFA, then mandatory password change) is checked on every `/oauth2/authorize` by `LoginWorkflowFilter`, not only after `POST /login`. A step is a `LoginWorkflowHandler` listed in order in `LoginWorkflowEngine`; a step page records its step as passed through `LoginWorkflowEngine.stepPassed`, and passed steps last for the session. See `docs/login-workflow.md` and ADR 0010.
- The Failed-Login Lock reuses the account's locked flag; `Account.lockedUntil` marks it temporary (ADR 0011). Change the lock state only through `Account.lock()` (lasting), `lockUntil(end)` (temporary) and `unlock()`, never `copy(accountNonLocked = ...)`, or a stale `lockedUntil` can unlock an admin lock. Failed logins are counted from the exact Spring `AuthenticationFailureBadCredentialsEvent` class (MFA and change-password publish subclasses); an expired lock is lifted in `AccountUserDetailsService`. Applying and lifting the lock go through `FailedLoginLockRepository` conditional updates (`JdbcFailedLoginLockRepository`), not a save of an account read earlier, so applying a lock never overwrites an admin or lifecycle lock and only one login publishes an expiry. Other account updates, such as an admin save or a password reset or change, still save the whole account and the last write wins: one that races a lock being applied can erase it. The PostgreSQL `ACCOUNT.locked_until` column needs a manual `ALTER TABLE` on existing databases (see `docs/failed-login-lock.md`).
- The logout support is RP-initiated OIDC logout plus session-management state cleanup. Do not describe `/oidc/logout` as OIDC Front-Channel Logout unless the front-channel metadata and iframe fan-out flow are implemented.

## Files Worth Reading First

- `pom.xml`
- `local/readme.md`
- `docs/profiles.md`
- `docs/management.md`
- `docs/mfa.md`
- `docs/login-workflow.md`
- `docs/password-lifecycle.md`
- `docs/failed-login-lock.md`
- `docs/lambda.md`
- `docs/adr/`
- `src/main/kotlin/com/vauthenticator/server/config/AuthorizationServerConfig.kt`
- `src/main/kotlin/com/vauthenticator/server/config/WebSecurityConfig.kt`
- `src/main/frontend/webpack.config.js`

## Practical Notes For Future Agents

- `application.yml` at the root resource level is intentionally minimal, so expect most behavior to come from profile-specific or component-specific configuration classes and local overrides.
- Scheduling is enabled with `@EnableScheduling` on `VAuthenticatorApplication`. The password lifecycle job runs on the `password.password-life-cycle.cron` Spring cron expression (six fields, seconds first; defaults to `0 0 * * * *`, every hour).
- The local helper scripts and docs are important in this project because a large part of the runtime setup lives outside the default Spring Boot process.
