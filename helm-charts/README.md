# VAuthenticator Helm Charts

This directory contains the Helm chart repository assets for the VAuthenticator ecosystem.

Two charts are published from this directory:

- `charts/vauthenticator`: VAuthenticator authorization server (`application`) and optional
  in-namespace Redis and PostgreSQL dependencies from Bitnami
- `charts/management-ui`: the standalone management UI static React SPA, served by nginx

## Usage

Helm must be installed to use the charts. Once Helm is available, add the chart repository:

```bash
helm repo add vauthenticator https://vauthenticator.github.io/helm-charts
helm repo update
helm search repo vauthenticator
```

For local chart development from this repository:

```bash
cd helm-charts
helm dependency update charts/vauthenticator
helm lint charts/vauthenticator --set ingress.host=localhost
helm template vauthenticator charts/vauthenticator --set ingress.host=localhost

helm lint charts/management-ui \
  --set application.idpBaseUrl=http://localhost \
  --set application.clientApplicationId=vauthenticator-management-ui \
  --set application.redirectUri=http://localhost/callback \
  --set application.authenticationCheckInterval=15000 \
  --set application.apiBaseUrl=http://localhost/api
helm template management-ui charts/management-ui \
  --set application.idpBaseUrl=http://localhost \
  --set application.clientApplicationId=vauthenticator-management-ui \
  --set application.redirectUri=http://localhost/callback \
  --set application.authenticationCheckInterval=15000 \
  --set application.apiBaseUrl=http://localhost/api
```

## Redis

The chart can install Redis in the same namespace when `in-namespace.redis.enabled=true`. The dependency is the Bitnami Redis chart. For advanced Redis settings, refer to the Bitnami chart documentation:

- https://github.com/bitnami/charts/tree/main/bitnami/redis

## Documentation

Detailed chart values are documented in [charts/README.md](charts/README.md) (vauthenticator)
and [charts/management-ui/README.md](charts/management-ui/README.md) (management-ui).
