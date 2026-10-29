#!/usr/bin/env bash
# Builds auth-server and management-ui from the working tree, starts a fresh
# e2e stack, provisions the default tenant and runs the Playwright suite.
#
# usage: ./run.sh [--keep] [playwright test args...]
#   --keep  leave the stack running afterwards; re-run tests with `npx playwright test`
set -euo pipefail

E2E_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$E2E_DIR")"

KEEP=false
if [[ "${1:-}" == "--keep" ]]; then
  KEEP=true
  shift
fi

compose() {
  docker compose -f "$E2E_DIR/docker-compose.yml" "$@"
}

echo "==> building auth-server"
(cd "$ROOT_DIR/auth-server/local" && bash build-frontend.sh)
(cd "$ROOT_DIR/auth-server" && ./mvnw -q clean package -DskipTests)

# the template scenarios save email/SMS templates: give the stack a writable copy of the documents, so a run
# never touches auth-server/dist; the container user is not the host user, hence the open permissions
rm -rf "$E2E_DIR/.documents"
cp -r "$ROOT_DIR/auth-server/dist" "$E2E_DIR/.documents"
chmod -R a+rwX "$E2E_DIR/.documents"

echo "==> building management-ui"
(cd "$ROOT_DIR/management-ui" && bash production-build.sh)

echo "==> starting a fresh e2e stack"
compose down -v --remove-orphans
if [[ "$KEEP" == false ]]; then
  trap 'compose down -v --remove-orphans' EXIT
fi
compose up -d --build

echo "==> waiting for auth-server"
for _ in $(seq 1 90); do
  if curl -fs http://localhost:9091/actuator/health > /dev/null; then
    break
  fi
  sleep 2
done
curl -fs http://localhost:9091/actuator/health > /dev/null || {
  compose logs auth-server
  echo "auth-server did not become healthy" >&2
  exit 1
}

echo "==> provisioning the default tenant"
curl -fs -X POST http://localhost:9091/actuator/tenant-setup

echo "==> running playwright"
cd "$E2E_DIR"
npm ci
npx playwright install chromium
npx playwright test "$@"
