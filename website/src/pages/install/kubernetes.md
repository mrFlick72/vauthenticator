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

## After installing

A new installation has no accounts, keys or pages yet. Call the tenant setup once, on the management port (`8081`):

```sh
curl -X POST http://<authorization-server>:8081/actuator/tenant-setup
```

It creates the first administrator account and client applications, the signing keys, the default email and SMS templates, and the default sign-in, MFA, signup, password and error pages. Run it on a new installation only: running it again replaces the templates and pages you have edited and resets the administrator client application's secret.

## Documents

Templates and pages live in the document store: an S3 bucket, or a directory with the file-system engine. With the file-system engine, the chart mounts a volume at the configured path, which must be absolute. By default it is an `emptyDir`, so its documents are lost when the pod is replaced; name a PersistentVolumeClaim in the chart values to keep them.

## Routing

Each chart can publish its service through an Ingress or through the Gateway API, with an HTTPRoute attached to a Gateway the chart creates. The authorization server chart's HTTPRoute can also attach to a Gateway already in your cluster, such as a shared Traefik gateway.

## Upgrading from 0.9

- The database schema is applied to an empty database only. Run the SQL in the 0.10 upgrade notes on your PostgreSQL database before upgrading.
- DynamoDB is no longer supported and its data is not migrated. Its chart values are removed, the caches have their own values and are off by default, and the default profile is `kms`.
- The tenant setup is not for an existing installation. Save the new SMS template through the management UI, and upload the default pages to the document store.

[Read the 0.10 upgrade notes](https://github.com/mrFlick72/vauthenticator/blob/main/changelog/0.10.md#warning-upgrade-notes).

## Configuration references

- [Chart repository overview](https://github.com/mrFlick72/vauthenticator/blob/main/helm-charts/README.md).
- [Authorization server chart configuration](https://github.com/mrFlick72/vauthenticator/blob/main/helm-charts/charts/README.md).
- [Management UI chart configuration](https://github.com/mrFlick72/vauthenticator/blob/main/helm-charts/charts/management-ui/README.md).

Begin with the [local setup](../local/) if you are evaluating the project for the first time.
