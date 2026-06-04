# config-manager

`config-manager` exposes the external runtime configuration consumed by the VAuthenticator management UI.

The service is intentionally small: it reads environment-backed configuration with Viper, validates the fields required by the UI, enables CORS for the configured management UI origin, and serves `GET /api/config`.

## Stack

- Go 1.25
- Gin
- Viper
- `gin-contrib/cors`

## Configuration

Configuration is read from environment variables. Set `CONFIG_MANAGER_ENV_FILE` to point the service at a specific `.env` file. If it is not set, a local `.env` file is used when present. Environment variables exported directly in the shell override values from the file.

```env
SERVER_ADDRESS=":8086"

MANAGEMENT_UI_SERVER_URL="http://local.management.vauthenticator.com:8085"

IDP_BASE_URL="http://local.api.vauthenticator.com:9090"
CLIENT_APPLICATION_ID="vauthenticator-management-ui"
REDIRECT_URI="http://local.management.vauthenticator.com:8085/callback"
AUTHENTICATION_CHECK_INTERVAL="15000"
API_BASE_URL="http://local.api.vauthenticator.com:9090/api"
```

`MANAGEMENT_UI_SERVER_URL` is used as the allowed CORS origin. Keep it aligned with the host that serves the management UI.

## Run

```bash
export CONFIG_MANAGER_ENV_FILE=.env.example
make run
```

For local development, you can still use `cp .env.example .env` and then `make run` without exporting `CONFIG_MANAGER_ENV_FILE`.

## Build And Test

```bash
make test
make build
make tidy
```

The direct Go equivalents are:

```bash
go test ./...
go build -o bin/config-manager ./cmd/config-manager
go mod tidy
```

## API

### `GET /api/config`

Returns the management UI configuration. No authentication is required.

```json
{
  "idpBaseUrl": "http://local.api.vauthenticator.com:9090",
  "clientApplicationId": "vauthenticator-management-ui",
  "redirectUri": "http://local.management.vauthenticator.com:8085/callback",
  "authenticationCheckInterval": "15000",
  "apiBaseUrl": "http://local.api.vauthenticator.com:9090/api"
}
```

The management UI caches this response in `sessionStorage` under the `appConfig` key and clears it during logout.

## Project Layout

- `cmd/config-manager`: executable entry point and graceful shutdown
- `internal/api`: Gin router and `/api/config` endpoint
- `internal/config`: environment loading, parsing, defaults, and validation
- `.env.example`: local configuration example
- `Makefile`: development commands
