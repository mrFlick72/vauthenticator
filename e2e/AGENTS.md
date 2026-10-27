# VAuthenticator e2e Agent Guide

## Purpose

Browser-driven end-to-end tests: Playwright drives the management UI against a real auth-server, both built
from the working tree. Use this guide for any work under `e2e`; it takes precedence over the root `AGENTS.md`
for this subtree.

## Stack

- `@playwright/test` with TypeScript, Chromium only
- its own npm package (`package.json` here), not shared with `management-ui/src`
- a dedicated `docker-compose.yml`: postgres, redis, localstack, mailhog, auth-server, management-ui

## How A Run Works

`run.sh` is the single entry point:

1. builds auth-server frontend assets (`auth-server/local/build-frontend.sh`) and the jar (`./mvnw clean package -DskipTests`)
2. builds the management UI (`management-ui/production-build.sh`)
3. `docker compose down -v` then `up -d --build`, so every run starts from an empty database
4. waits for `:9091/actuator/health`, then `POST :9091/actuator/tenant-setup`
5. `npx playwright test`, then tears the stack down (skipped with `--keep`)

Playwright runs on the host against the published ports, so the browser uses the same hostnames as the
seeded client applications (`local.api.vauthenticator.com:9090`, `local.management.vauthenticator.com:8085`).
The hosts entries are a prerequisite, see `README.md`.

## Conventions For Changes

- Keep e2e-only configuration here: `auth-server/application.yml` and `management-ui.env` mirror the local
  development config with compose service names. Do not change `auth-server/local` or `management-ui/local` for e2e needs.
- Tests must not depend on each other or on data from a previous run: use unique ids (`e2e-<timestamp>`).
- Log in through the `setup` project and its storage state; do not drive the login form in every test.
- Prefer locating elements by the ids and labels the UI already renders. If a test needs a UI change to be
  locatable, that is a `management-ui` change and must be called out as cross-project.
- `storageState` does not capture `sessionStorage`: do not try to reuse management UI tokens across tests.

## Commands

From `e2e`:

- `./run.sh` / `./run.sh --keep`
- `npx playwright test` (against a kept stack)
- `npm run typecheck`
