# VAuthenticator Config Manager Agent Guide

## Purpose

`config-manager` is a small Go service that exposes runtime configuration for the standalone VAuthenticator management UI.

Use this guide for any work under `config-manager`. Per the repo root instructions, this file takes precedence over the monorepo-level `AGENTS.md` for this subtree.

The relevant skills if available for this project include: $golang-documentation $golang-patterns $golang-testing

## Stack

- Go 1.25
- Gin HTTP router
- Viper configuration loading
- `gin-contrib/cors` for CORS
- Standard `net/http` server with graceful shutdown

## Repository Layout

- `cmd/config-manager`: executable entry point
- `internal/api`: Gin router and HTTP endpoint tests
- `internal/config`: environment and `.env` file loading, parsing, and validation
- `.env.example`: local configuration example
- `Makefile`: build, run, test, and tidy shortcuts

## Runtime Contract

The service exposes one unauthenticated endpoint:

- `GET /api/config`

It returns the configuration consumed by `management-ui/src/config/ConfigLoader.ts`:

- `idpBaseUrl`
- `clientApplicationId`
- `redirectUri`
- `authenticationCheckInterval`
- `apiBaseUrl`

The management UI caches this response in `sessionStorage` under `appConfig` and clears it during logout.

## Configuration

Configuration is read from environment variables with Viper. If `CONFIG_MANAGER_ENV_FILE` is set, that file is required. Otherwise `.env` is used when present and direct shell environment variables still win.

Required values:

- `MANAGEMENT_UI_SERVER_URL`
- `IDP_BASE_URL`
- `CLIENT_APPLICATION_ID`
- `REDIRECT_URI`
- `AUTHENTICATION_CHECK_INTERVAL`
- `API_BASE_URL`

Optional value:

- `SERVER_ADDRESS` defaults to `:8086`

`MANAGEMENT_UI_SERVER_URL` is the allowed CORS origin. Keep this aligned with the management UI host.

## Build And Test Commands

- `make test`
- `make run`
- `make build`
- `make tidy`

Equivalent direct commands:

- `go test ./...`
- `go run ./cmd/config-manager`
- `go build -o bin/config-manager ./cmd/config-manager`
- `go mod tidy`

## Conventions For Changes

- Keep the executable entry point thin; put HTTP behavior in `internal/api` and configuration rules in `internal/config`.
- Keep the `/api/config` response shape aligned with `management-ui/src/config/ConfigLoader.ts`.
- If configuration fields are added, update `.env.example`, `README.md`, tests, and the management UI contract docs together.
- Keep CORS explicit. Do not replace the configured management UI origin with a wildcard without an explicit security decision.
- Do not add authentication to `/api/config` unless the management UI bootstrap flow is changed at the same time.
- Prefer table-driven tests for config parsing and HTTP handler behavior.

## Files Worth Reading First

- `cmd/config-manager/main.go`
- `internal/api/router.go`
- `internal/config/config.go`
- `README.md`
- `.env.example`
