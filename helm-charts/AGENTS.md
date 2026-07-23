# VAuthenticator Helm Chart Agent Guide

## Purpose

`helm-charts` contains the Helm packaging for deploying VAuthenticator workloads to Kubernetes.

Use this guide for any work under `helm-charts`. Per the repo root instructions, this file takes precedence over the monorepo-level `AGENTS.md` for this subtree.

The relevant skill if available for this project is: $helm-chart-patterns

## Stack

- Helm 3 application chart
- Kubernetes manifests rendered from Go templates
- Optional Bitnami Redis dependency controlled by `in-namespace.redis.enabled`
- Optional KEDA `ScaledObject` resource for `application`

## Repository Layout

This directory holds three independent charts.

- `README.md`: chart repository usage
- `charts/README.md`: values and chart configuration documentation for `charts/vauthenticator`
- `charts/vauthenticator/Chart.yaml`: chart metadata and dependencies
- `charts/vauthenticator/values.yaml`: default values
- `charts/vauthenticator/templates`: rendered Kubernetes resources, including the workload `ConfigMap` in `configmap.yaml` and the `Deployment` in `deployment.yaml`
- `changelog`: `charts/vauthenticator` release notes
- `charts/config-manager/Chart.yaml`, `charts/config-manager/values.yaml`, `charts/config-manager/templates`: the `config-manager` chart
- `charts/config-manager/README.md`: values and chart configuration documentation for `charts/config-manager`
- `charts/config-manager/changelog`: `charts/config-manager` release notes
- `charts/management-ui/Chart.yaml`, `charts/management-ui/values.yaml`, `charts/management-ui/templates`: the `management-ui` chart
- `charts/management-ui/README.md`: values and chart configuration documentation for `charts/management-ui`
- `charts/management-ui/changelog`: `charts/management-ui` release notes

## Workloads

`charts/vauthenticator` renders:

- `application`: the VAuthenticator authorization server
- optional in-namespace Redis subchart

`charts/config-manager` renders the `config-manager` Go service (Deployment + ClusterIP Service
only, no Ingress — see `charts/config-manager/README.md`). `config-manager`'s source lives at the
repository root, separate from this chart.

`charts/management-ui` renders the management UI static React SPA (Deployment + ClusterIP
Service + optional Ingress/HTTPRoute — see `charts/management-ui/README.md`). The image is an
nginx container built from the `management-ui` project's `Dockerfile`, which also proxies
`/api/config` to `config-manager` at request time so the browser never needs to know where
`config-manager` actually lives (see `management-ui/docker/default.conf.template`).

## Build And Validation Commands

From `helm-charts`:

- `helm dependency update charts/vauthenticator`
- `helm lint charts/vauthenticator --set ingress.host=localhost`
- `helm template vauthenticator charts/vauthenticator --set ingress.host=localhost`
- `helm lint charts/config-manager --set application.managementUiServerUrl=http://localhost`
- `helm template config-manager charts/config-manager --set application.managementUiServerUrl=http://localhost`
- `helm lint charts/management-ui --set application.configManagerUpstream=http://localhost:8086`
- `helm template management-ui charts/management-ui --set application.configManagerUpstream=http://localhost:8086`

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
- `charts/vauthenticator/templates/deployment.yaml`
- `charts/vauthenticator/templates/configmap.yaml`
- `charts/vauthenticator/templates/_helpers.tpl`
- `charts/README.md`
