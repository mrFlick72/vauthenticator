# VAuthenticator Management UI Agent Guide

## Purpose

This project is the standalone admin UI for VAuthenticator. It is a React and TypeScript application that builds static bundles for the management experience, including the admin SPA plus OAuth callback and RP-initiated logout entry points.

Use this guide for any work under `management-ui`. Per the repo root instructions, this file takes precedence over the monorepo-level `AGENTS.md` for this subtree.

## Stack

- React 19
- TypeScript 6
- webpack 5
- Material UI 9
- React Router 7
- Runtime auth/API settings are loaded from a static `/config.json` generated at container start

The package manifest is `src/package.json` and the webpack config is `src/webpack.config.js`.

## Repository Layout

- `src/admin`: admin SPA routes and feature pages
- `src/auth`: OAuth callback, logout, authentication helpers, and OIDC session-management iframe integration
- `src/components`: shared UI components used across pages
- `src/config`: runtime config loading from `GET /config.json`
- `src/theme`: MUI theme setup
- `src/utils`: shared frontend utilities
- `local`: docker-compose file and local client app provisioning for local static serving
- `docker`: production nginx config template, `config.json.template`, and the entrypoint script used by the root `Dockerfile`
- `dist`: generated frontend output
- `changelog`: project release notes

## App Shape

Webpack builds three entry points:

- `admin` -> `src/admin/index.tsx`
- `callback` -> `src/auth/Callback.tsx`
- `logout` -> `src/auth/Logout.tsx`

The admin app uses `BrowserRouter` with `basename="/secure/admin"`. Current top-level routes include:

- home
- client applications list/create/edit
- roles
- accounts list/edit
- keys
- email templates

The home screen is a card-based navigation page that links into those feature areas.

## Authentication And Backend Coupling

Authentication is handled in `src/auth/Authenticator.ts` using an OAuth2 authorization code flow with PKCE. Tokens are stored in `window.sessionStorage`.

Logout in this UI is RP-initiated OIDC logout against the authorization server. It is not an OIDC Front-Channel Logout receiver.

Runtime configuration is loaded by `src/config/ConfigLoader.ts` from `GET /config.json`, a static file generated at container start (see "Production Image" below). The response is cached in `window.sessionStorage` under `appConfig` and cleared on logout.

OIDC Session Management is implemented in `src/auth/SessionManagement.tsx`. It uses the OP iframe at `${idpBaseUrl}/session/management`, stores the current `SESSION_STATE`, and shows a relogin dialog when the OP reports `changed` or `error` and the silent session check fails.

Important session keys:

- `ID_TOKEN`
- `ACCESS_TOKEN`
- `SESSION_STATE`
- `codeVerifier`
- `returnTo`
- `appConfig`

The UI depends on the VAuthenticator backend for:

- `/oauth2/authorize`
- `/oauth2/token`
- `/userinfo`
- `/connect/logout`
- `/session/management`
- `/api/...` management endpoints used by repositories under `src/admin`

When changing admin pages, always check the corresponding repository class to confirm the backend contract rather than inferring request and response shapes from UI usage alone.

## Environment And Local Runtime

Runtime configuration comes from `GET /config.json`, a static file generated inside the
container at start time from environment variables (see "Production Image" below).

Local dev runs the actual project image via `local/docker-compose.yml` (`build:` against
the root `Dockerfile`), so it exercises the same entrypoint/template code path as
production and Helm. Values come from `local/.env` (copy `local/.env.example`):

- `REDIRECT_URI`
- `CLIENT_APPLICATION_ID`
- `IDP_BASE_URL`
- `AUTHENTICATION_CHECK_INTERVAL`
- `API_BASE_URL`

Local defaults point to:

- management UI: `http://local.management.vauthenticator.com:8085`
- auth server/API: `http://local.api.vauthenticator.com:9090`

## Production Image

The root `Dockerfile` builds the SPA (`npm run production-build`) and copies it into an
`nginx:1.27-alpine` image. `docker/default.conf.template` is the production nginx config,
rendered at container start via the official nginx image's envsubst-on-templates
mechanism. A second startup step, `docker/40-generate-app-config.sh` (installed into
`/docker-entrypoint.d/`, the base image's own extension hook), runs `envsubst` over
`docker/config.json.template` to produce `/usr/share/nginx/html/config.json` from the five
env vars above — sourced from a ConfigMap in the `helm-charts/charts/management-ui` chart
via `envFrom`. `GET /config.json` is served with `Cache-Control: no-store` so a redeploy's
new values are never served stale. See `helm-charts/charts/management-ui/README.md` for
the chart side.

## Build And Run Commands

From `management-ui/src`:

- `npm install`
- `npm run build`
- `npm run production-build`
- `npm run watch`

From `management-ui`:

- `bash build.sh`

Notes:

- `build.sh` removes `dist/`, reinstalls `src/node_modules`, and runs the development webpack build.
- The generated output is written to `management-ui/dist`.
- For the local UI to start authentication correctly, set `local/.env` (from `local/.env.example`) to match the local auth server before running `docker compose up --build` in `local/`.

## Conventions For Changes

- Keep feature logic inside the existing admin domains such as `account`, `clientapp`, `communication`, `key`, and `roles`.
- Reuse shared components from `src/components` before introducing new one-off widgets.
- Preserve the existing routing style with `BrowserRouter` (basename `/secure/admin`) unless a broader routing migration is explicitly requested. Internal navigation must go through `react-router`'s `Link`/`useNavigate` (which respect the basename automatically) rather than raw `<a href>` tags, which bypass it.
- Keep runtime values in the `/config.json` response and `src/config/ConfigLoader.ts`; do not hardcode backend hosts or client IDs into feature components.
- When changing authentication, token usage, or OIDC session management, verify the callback, logout, config cache, and session-storage flow together.
- If a page fetches backend data, inspect the matching repository file first and keep request/response changes aligned with backend APIs.
- Avoid mixing `management-ui` edits with `auth-server` or Helm changes unless explicitly requested.

## Files Worth Reading First

- `src/webpack.config.js`
- `src/admin/index.tsx`
- `src/auth/Authenticator.ts`
- `src/auth/SessionManagement.tsx`
- `src/config/ConfigLoader.ts`
- `local/docker-compose.yml`
- `docker/default.conf.template`
- `docker/config.json.template`

## Practical Notes For Future Agents

- This project already contains generated output in `dist/`; do not edit generated files by hand.
- `src/node_modules` is present inside the project tree, so be careful to avoid noisy searches or accidental edits there.
- The auth check interval value is multiplied by `1000` in `Authenticator.ts`, so verify the intended unit before changing it.
