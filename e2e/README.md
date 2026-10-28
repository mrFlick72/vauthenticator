# VAuthenticator e2e tests

Browser-driven [Playwright](https://playwright.dev) tests that run the management UI against a real auth-server.

## Prerequisites

- Docker with the compose plugin
- JDK 25 and Node 22
- the local hosts entries already needed for local development:

  ```
  127.0.0.1   local.api.vauthenticator.com
  127.0.0.1   local.management.vauthenticator.com
  ```

- ports `9090`, `9091` and `8085` free (a locally running auth-server or management UI must be stopped;
  the infrastructure containers of `auth-server/local` can keep running, the e2e stack does not publish their ports)

## Run

```shell
./run.sh
```

`run.sh` builds auth-server and management-ui from the working tree, starts a fresh stack from
`docker-compose.yml` (postgres, redis, localstack, mailhog, auth-server, management-ui), provisions the
default tenant via `POST :9091/actuator/tenant-setup`, runs the suite and tears the stack down.
Extra arguments are passed to `playwright test`.

While writing tests keep the stack up and re-run only Playwright:

```shell
./run.sh --keep
npx playwright test          # or: npx playwright test --ui
docker compose down -v       # when done
```

## Layout

- `docker-compose.yml`: the e2e stack; postgres has no volume and loads `auth-server/src/main/resources/data/schema.sql` at start
- `auth-server/application.yml`: auth-server configuration for the stack, mounted as `config/application.yml`
- `management-ui.env`: runtime configuration for the management UI container
- `tests/admin-login.setup.ts`: logs in as the seeded `admin@email.com` and saves the browser storage state
- `pages/`: page objects, one per management UI or auth-server screen; they own locators and actions, specs own assertions
- `support/`: test data builders (`aClientApplication()`) and direct auth-server calls used to prove a result
- `tests/*.spec.ts`: scenarios, written against the page objects

## Login and storage state

The setup project logs in once and saves Playwright's `storageState`. That captures the auth-server session
cookie but not `sessionStorage`, where the management UI keeps its tokens: each test still goes through
`/oauth2/authorize`, and the cookie lets it through without the login form.

## CI

`.github/workflows/e2e.yml` runs `./run.sh` on pull requests and uploads the Playwright report.
