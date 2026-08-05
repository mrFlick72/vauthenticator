# VAuthenticator

VAuthenticator is an OpenID Connect and OAuth2 authorization server ecosystem. The repository started from an OAuth2 authorization server built during a master thesis and now contains the backend, admin UI, and Helm deployment assets.

## Projects

| Project | Purpose |
| --- | --- |
| `auth-server` | Kotlin/Spring Boot authorization server with OAuth2, OIDC, MFA, account lifecycle, roles, keys, templates, and management APIs. |
| `management-ui` | Standalone React/TypeScript admin UI for managing clients, accounts, roles, keys, and email templates. |
| `helm-charts` | Helm chart and chart repository docs for Kubernetes deployment. |

## Architecture

![](https://github.com/mrFlick72/vauthenticator/blob/main/images/vauthenticator-architecture.png)

## Features

Backend capabilities include:

- OAuth2 Authorization Server and OpenID Connect support on Spring Boot 4.x and Spring Security 7+
- JWT access token and ID token customization through Lambda integration
- Client application, role, account, and key management APIs
- Signup, welcome email, email verification, password reset, and password change flows
- MFA with email, SMS, and OTP support
- Post-login workflows, including forced password reset
- RP-initiated logout and OIDC Session Management
- Custom actuator management endpoints for setup and cleanup

Storage and infrastructure options include:

- DynamoDB or PostgreSQL persistence profiles
- Redis for authorization code storage, distributed session storage, and cache
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
docker compose -f local/docker-compose.yml up
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
- [Auth server Lambda token customization](auth-server/docs/lambda.md)
- [Auth server management endpoints](auth-server/docs/management.md)
- [Auth server security assessment](auth-server/docs/security-assessment.md)
- [Management UI agent guide](management-ui/AGENTS.md)
- [Helm chart README](helm-charts/README.md)
