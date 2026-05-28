# VAuthenticator Helm Charts

This directory contains the Helm chart repository assets for the VAuthenticator ecosystem.

The current chart deploys:

- VAuthenticator authorization server (`application`)
- VAuthenticator management UI workload (`managementUi`)
- optional in-namespace Redis dependency from Bitnami

`config-manager` is a separate Go project in this monorepo and is not currently rendered by the chart.

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
helm lint charts/vauthenticator --set application.ingress.host=localhost --set managementUi.ingress.host=localhost
helm template vauthenticator charts/vauthenticator --set application.ingress.host=localhost --set managementUi.ingress.host=localhost
```

## Redis

The chart can install Redis in the same namespace when `in-namespace.redis.enabled=true`. The dependency is the Bitnami Redis chart. For advanced Redis settings, refer to the Bitnami chart documentation:

- https://github.com/bitnami/charts/tree/main/bitnami/redis

## Documentation

Detailed chart values are documented in [charts/README.md](charts/README.md).
