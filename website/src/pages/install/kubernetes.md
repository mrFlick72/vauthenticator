---
layout: ../../layouts/DocsLayout.astro
title: Kubernetes & Helm
description: Explore the separate Helm charts for the authorization server and management UI.
section: Get started · Kubernetes
---

## Two application charts

The chart repository includes a chart for the VAuthenticator authorization server and a separate chart for the management UI. Each has its own configuration values.

## Add the chart repository

```sh
helm repo add vauthenticator https://mrflick72.github.io/vauthenticator
helm repo update
helm search repo vauthenticator
```

This lists the available charts. A working deployment also needs server infrastructure and profile configuration, management UI runtime settings, and your cluster's networking configuration.

## Configuration references

- [Chart repository overview](https://github.com/mrFlick72/vauthenticator/blob/main/helm-charts/README.md).
- [Authorization server chart configuration](https://github.com/mrFlick72/vauthenticator/blob/main/helm-charts/charts/README.md).
- [Management UI chart configuration](https://github.com/mrFlick72/vauthenticator/blob/main/helm-charts/charts/management-ui/README.md).

Begin with the [local setup](/vauthenticator/install/local/) if you are evaluating the project for the first time.
