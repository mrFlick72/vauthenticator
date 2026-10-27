# VAuthenticator

VAuthenticator is an OpenID Connect and OAuth2 authorization server ecosystem. The repository started from an OAuth2 authorization server built during a master thesis and now contains the backend, admin UI, and Helm deployment assets.

## Projects

| Project | Purpose |
| --- | --- |
| `auth-server` | Kotlin/Spring Boot authorization server with OAuth2, OIDC, MFA, account lifecycle, roles, keys, templates, and management APIs. |
| `management-ui` | Standalone React/TypeScript admin UI for managing clients, accounts, roles, keys, email and SMS templates, and password lifecycle rules. |
| `helm-charts` | Helm chart and chart repository docs for Kubernetes deployment. |

## Architecture

![](https://github.com/mrFlick72/vauthenticator/blob/main/images/vauthenticator-architecture.png)

## Features

Backend capabilities include:

- OAuth2 Authorization Server and OpenID Connect support on Spring Boot 4.x and Spring Security 7+
- JWT access token and ID token customization through Lambda integration
- Client application, role, account, and key management APIs
- Signup, welcome email, email verification, password reset, and password change flows
- MFA with one-time codes sent by email or SMS, with self-service device management (enrol, associate, choose the default, revoke)
- Post-login workflow (MFA, then a mandatory password change), checked at every authorization request
- Failed-Login Lock: after too many consecutive wrong passwords (default 5) an account is locked for a while (default 1 minute) (see [auth-server/docs/failed-login-lock.md](auth-server/docs/failed-login-lock.md))
- Password lifecycle rules (PostgreSQL profile): force a password reset every N seconds (recurring) or lock an account once N seconds have passed (one-shot), for a single account or in bulk for every account matching a pattern such as `*@gmail.com` (see [auth-server/docs/password-lifecycle.md](auth-server/docs/password-lifecycle.md))
- RP-initiated logout and OIDC Session Management
- Custom actuator management endpoints for setup and cleanup

Storage and infrastructure options include:

- PostgreSQL persistence
- Redis for distributed session storage, opt-in account/role/client application caches, and failed-login counts
- AWS KMS-backed keys or local/plain Java key management
- S3 or filesystem-backed document/static asset loading

## Local Development

Add these hostnames to your local hosts file:

```text
127.0.0.1   local.api.vauthenticator.com
127.0.0.1   local.management.vauthenticator.com
```

Start the auth-server dependencies and tenant setup from the auth-server local docs:

- [auth-server/local/readme.md](auth-server/local/readme.md)

Build and serve the management UI locally:

```bash
cd management-ui
bash build.sh
docker compose -f local/docker-compose.yml up --build
```

The local UI is served from:

- `http://local.management.vauthenticator.com:8085/secure/admin/index`

Runtime auth/API settings are loaded from a static `GET /config.json`, generated at container start by the nginx entrypoint from `local/.env` (see `management-ui/AGENTS.md`).

## Build And Test

| Project | Commands |
| --- | --- |
| `auth-server` | `./mvnw test`, `./mvnw package`, `./mvnw spring-boot:run` |
| `auth-server/src/main/frontend` | `npm install`, `npm run build`, `npm run production-build`, `npm run watch` |
| `management-ui/src` | `npm install`, `npm run build`, `npm run production-build`, `npm run watch` |
| `management-ui` | `bash build.sh` |

## Documentation

- [Auth server profiles](auth-server/docs/profiles.md)
- [Auth server MFA](auth-server/docs/mfa.md)
- [Auth server MFA sequence diagrams](auth-server/docs/mfa-sequence-diagrams.md)
- [Auth server login workflow](auth-server/docs/login-workflow.md)
- [Auth server Failed-Login Lock](auth-server/docs/failed-login-lock.md)
- [Auth server password lifecycle](auth-server/docs/password-lifecycle.md)
- [Auth server Lambda token customization](auth-server/docs/lambda.md)
- [Auth server management endpoints](auth-server/docs/management.md)
- [Auth server security assessment](auth-server/docs/security-assessment.md)
- [Management UI agent guide](management-ui/AGENTS.md)
- [Helm chart README](helm-charts/README.md)
