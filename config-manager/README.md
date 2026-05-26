# config-manager

`config-manager` exposes the external runtime configuration consumed by the VAuthenticator management UI.

## Configuration

Configuration is read with Viper from environment variables. Set `CONFIG_MANAGER_ENV_FILE` to point the service at a specific `.env` file. If it is not set, a local `.env` file is used when present.
Environment variables exported directly in the shell override values from the file.

```env
SERVER_ADDRESS=":8086"

MANAGEMENT_UI_SERVER_URL="http://local.management.vauthenticator.com:8085"

IDP_BASE_URL="http://local.api.vauthenticator.com:9090"
CLIENT_APPLICATION_ID="vauthenticator-management-ui"
REDIRECT_URI="http://local.management.vauthenticator.com:8085/callback"
AUTHENTICATION_CHECK_INTERVAL="15000"
API_BASE_URL="http://local.api.vauthenticator.com:9090/api"
```

`MANAGEMENT_UI_SERVER_URL` is used as the allowed CORS origin.

## Run

```bash
export CONFIG_MANAGER_ENV_FILE=.env.example
make run
```

For local development, you can still use `cp .env.example .env` and then `make run` without exporting `CONFIG_MANAGER_ENV_FILE`.

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

The management UI should cache this response in `sessionStorage` under the `appConfig` key and clear it during logout.
