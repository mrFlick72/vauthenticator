# VAuthenticator Management UI Agent Guide

## Purpose

This project is the standalone admin UI for VAuthenticator. It is a React and TypeScript application that builds static bundles for the management experience, including the admin SPA plus OAuth callback and logout entry points.

Use this guide for any work under `management-ui`. Per the repo root instructions, this file takes precedence over the monorepo-level `Agents.md` for this subtree.

## Stack

- React 18
- TypeScript
- webpack 5
- Material UI 5
- React Router 6
- `dotenv-webpack` for build-time environment injection

The package manifest is `src/package.json` and the webpack config is `src/webpack.config.js`.

## Repository Layout

- `src/admin`: admin SPA routes and feature pages
- `src/auth`: OAuth callback, logout, and authentication helpers
- `src/components`: shared UI components used across pages
- `src/config`: runtime config loading from build-time env vars
- `src/theme`: MUI theme setup
- `src/utils`: shared frontend utilities
- `environments`: `.env.*` files consumed by webpack builds
- `local`: nginx and docker-compose files for local static serving
- `dist`: generated frontend output
- `changelog`: project release notes

## App Shape

Webpack builds three entry points:

- `admin` -> `src/admin/index.tsx`
- `callback` -> `src/auth/Callback.tsx`
- `logout` -> `src/auth/Logout.tsx`

The admin app uses `HashRouter`. Current top-level routes include:

- home
- client applications list/create/edit
- roles
- accounts list/edit
- keys
- email templates

The home screen is a card-based navigation page that links into those feature areas.

## Authentication And Backend Coupling

Authentication is handled in `src/auth/Authenticator.ts` using an OAuth2 authorization code flow with PKCE. Tokens are stored in `window.sessionStorage`.

Important session keys:

- `ID_TOKEN`
- `ACCESS_TOKEN`
- `codeVerifier`
- `returnTo`

The UI depends on the VAuthenticator backend for:

- `/oauth2/authorize`
- `/oauth2/token`
- `/userinfo`
- `/connect/logout`
- `/api/...` management endpoints used by repositories under `src/admin`

When changing admin pages, always check the corresponding repository class to confirm the backend contract rather than inferring request and response shapes from UI usage alone.

## Environment And Local Runtime

The development build reads `environments/.env.development`.

Current development variables include:

- `REDIRECT_URI`
- `CLIENT_APPLICATION_ID`
- `IDP_BASE_URL`
- `AUTHENTICATION_CHECK_INTERVAL`
- `API_BASE_URL`

Local defaults point to:

- management UI: `http://local.management.vauthenticator.com:8085`
- auth server/API: `http://local.api.vauthenticator.com:9090`

Local serving is nginx-based through `local/docker-compose.yml`, which mounts `dist/` into an nginx container and exposes port `8085`.

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

## Conventions For Changes

- Keep feature logic inside the existing admin domains such as `account`, `clientapp`, `communication`, `key`, and `roles`.
- Reuse shared components from `src/components` before introducing new one-off widgets.
- Preserve the existing routing style with `HashRouter` unless a broader routing migration is explicitly requested.
- Keep environment-driven values in `src/config/ConfigLoader.ts`; do not hardcode backend hosts or client IDs into feature components.
- When changing authentication or token usage, verify the callback, logout, and session-storage flow together.
- If a page fetches backend data, inspect the matching repository file first and keep request/response changes aligned with backend APIs.
- Avoid mixing `management-ui` edits with `auth-server` or Helm changes unless explicitly requested.

## Files Worth Reading First

- `src/webpack.config.js`
- `src/admin/index.tsx`
- `src/auth/Authenticator.ts`
- `src/config/ConfigLoader.ts`
- `local/docker-compose.yml`
- `local/conf.d/default.conf`

## Practical Notes For Future Agents

- This project already contains generated output in `dist/`; do not edit generated files by hand.
- `src/node_modules` is present inside the project tree, so be careful to avoid noisy searches or accidental edits there.
- The auth check interval value is multiplied by `1000` in `Authenticator.ts`, so verify the intended unit before changing it.
