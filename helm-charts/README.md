# VAuthenticator Helm Charts

This directory contains the Helm chart repository assets for the VAuthenticator ecosystem.

Two charts are published from this directory:

- `charts/vauthenticator`: VAuthenticator authorization server (`application`), management UI
  workload (`managementUi`), and an optional in-namespace Redis dependency from Bitnami
- `charts/config-manager`: the `config-manager` runtime-config service consumed by the management UI

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

helm lint charts/config-manager --set application.managementUiServerUrl=http://localhost
helm template config-manager charts/config-manager --set application.managementUiServerUrl=http://localhost
```

## Redis

The chart can install Redis in the same namespace when `in-namespace.redis.enabled=true`. The dependency is the Bitnami Redis chart. For advanced Redis settings, refer to the Bitnami chart documentation:

- https://github.com/bitnami/charts/tree/main/bitnami/redis

## Documentation

Detailed chart values are documented in [charts/README.md](charts/README.md) (vauthenticator) and
[charts/config-manager/README.md](charts/config-manager/README.md) (config-manager).
