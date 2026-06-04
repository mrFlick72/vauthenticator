# VAuthenticator Helm Chart Agent Guide

## Purpose

`helm-charts` contains the Helm packaging for deploying VAuthenticator workloads to Kubernetes.

Use this guide for any work under `helm-charts`. Per the repo root instructions, this file takes precedence over the monorepo-level `AGENTS.md` for this subtree.

The relevant skill if available for this project is: $helm-chart-patterns

## Stack

- Helm 3 application chart
- Kubernetes manifests rendered from Go templates
- Optional Bitnami Redis dependency controlled by `in-namespace.redis.enabled`
- Optional KEDA `ScaledObject` resources for `application` and `managementUi`

## Repository Layout

- `README.md`: chart repository usage
- `charts/README.md`: values and chart configuration documentation
- `charts/vauthenticator/Chart.yaml`: chart metadata and dependencies
- `charts/vauthenticator/values.yaml`: default values
- `charts/vauthenticator/templates`: rendered Kubernetes resources
- `changelog`: chart release notes

## Workloads

The current chart renders:

- `application`: the VAuthenticator authorization server
- `managementUi`: the management UI application image configured by the chart templates
- optional in-namespace Redis subchart

`config-manager` is a separate Go project at the repository root and is not currently templated by this chart unless a change explicitly adds that deployment.

## Build And Validation Commands

From `helm-charts`:

- `helm dependency update charts/vauthenticator`
- `helm lint charts/vauthenticator --set application.ingress.host=localhost --set managementUi.ingress.host=localhost`
- `helm template vauthenticator charts/vauthenticator --set application.ingress.host=localhost --set managementUi.ingress.host=localhost`

## Conventions For Changes

- Keep `values.yaml`, templates, and `charts/README.md` in sync whenever a value is added, renamed, or removed.
- Bump `Chart.yaml` chart version and add a changelog entry for chart behavior changes.
- Do not put secrets directly in default values; document secret integration or value overrides instead.
- Preserve existing value names unless the migration is explicitly requested. Some keys, such as `lables`, are misspelled but part of the current chart API.
- Use helpers from `_helpers.tpl` for names, labels, and selector labels.
- Quote strings that may be parsed ambiguously by YAML, and use `toYaml` plus `nindent` for nested maps.
- Validate rendered manifests with `helm lint` and, when changing templates, `helm template`.
- The default ingress host is `*`; pass explicit host values when linting or rendering locally.

## Files Worth Reading First

- `charts/vauthenticator/values.yaml`
- `charts/vauthenticator/templates/vauthenticator.yaml`
- `charts/vauthenticator/templates/vauthenticator-management-ui.yaml`
- `charts/vauthenticator/templates/_helpers.tpl`
- `charts/README.md`
