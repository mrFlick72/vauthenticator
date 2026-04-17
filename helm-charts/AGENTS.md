# Repository Guidelines

## Project Structure & Module Organization
This directory is the Helm subproject of the VAuthenticator monorepo. Keep Helm-only changes in `helm-charts/`; do not mix edits from `../auth-server` or `../management-ui` unless explicitly requested. The installable chart lives in `charts/vauthenticator/`: `Chart.yaml` defines metadata and dependencies, `values.yaml` holds defaults, and `templates/` contains rendered Kubernetes resources such as `vauthenticator.yaml`, `vauthenticator-management-ui.yaml`, and `serviceaccount.yaml`. Top-level `README.md` covers chart usage, `charts/README.md` documents values, and `changelog/` stores release notes by version.

Use whenever is required context7 MCP server and the following skills if available: 
- $kubernetes-architect
- $helm-chart-patterns
- $helm-chart-scaffolding
- $helm-expert

## Build, Test, and Development Commands
- `helm dependency update charts/vauthenticator` fetches the Bitnami Redis dependency declared in `Chart.yaml`.
- `helm lint charts/vauthenticator` validates chart structure and template syntax.
- `helm template dev charts/vauthenticator -f charts/vauthenticator/values.yaml` renders manifests locally for review.
- `helm package charts/vauthenticator` builds a distributable chart archive.

Run lint and template before opening a PR. If you change dependencies or defaults, rerun `helm dependency update`.

## Coding Style & Naming Conventions
Use 2-space YAML indentation. Keep Helm control blocks trimmed with `{{- ... -}}` where appropriate, and reuse shared helpers from `charts/vauthenticator/templates/_helpers.tpl` for names and labels. Preserve the existing values schema instead of renaming keys; examples include `managementUi`, `selectorLabels`, and `in-namespace`. Follow current naming patterns: lower-kebab-case Kubernetes resource names and `vauthenticator.*` helper identifiers.

## Testing Guidelines
This repo has no unit-test suite; validation is chart-focused. Every change should pass `helm lint` and a local `helm template` render. Review both authorization server and management UI resources when editing shared values or helpers. If a change affects chart behavior or exposed configuration, update `charts/README.md` and add or extend the matching file in `changelog/`.

## Commit & Pull Request Guidelines
Current history uses short, imperative commit subjects without prefixes, for example `add management ui and helm chart resources as raw copy`. Keep commits small and scoped to `helm-charts/`. PRs should explain operator impact, list changed values, note any version bump in `charts/vauthenticator/Chart.yaml`, and include rendered manifest snippets when behavior changes are not obvious from the diff.

## Security & Configuration Tips
Never commit real AWS keys, secrets, or environment-specific endpoints in `values.yaml`. Keep placeholders in source control and provide real settings through separate values files, secret managers, or deployment-time overrides.
