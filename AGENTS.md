This repository is a monorepo with 4 projects:

- auth-server -> Kotlin OAuth2/OIDC Authorization Server
- management-ui -> React/TypeScript admin UI
- helm-charts -> Helm chart deployment assets
- e2e -> Playwright end-to-end tests driving management-ui against auth-server

Always use the closest AGENTS.md file. Root instructions apply only when a subtree does not have its own guide or when the work is explicitly cross-project.

Project boundaries:

- `auth-server`: backend authorization server, local auth-server assets, and auth-server docs
- `management-ui`: standalone React admin UI, nginx local serving config, and UI docs
- `helm-charts`: Kubernetes/Helm packaging and chart documentation
- `e2e`: end-to-end test suite, its compose stack and e2e-only configuration; it builds `auth-server` and `management-ui` from the working tree; a fix in their sources surfaced by e2e is a cross-project change and must be called out

Never mix changes across projects unless explicitly requested. If a contract must change across projects, state that coupling clearly and update the related docs in the same change.

Do not edit generated outputs by hand, including `auth-server/target`, `management-ui/dist`, `auth-server/dist`, dependency directories, or packaged Helm chart archives.
