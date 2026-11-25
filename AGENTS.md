This repository is a monorepo with 6 projects:

- platform -> the starting point of every VAuthenticator application: the shared parent POM and the jar of mechanisms the servers are built on
- auth-server -> Kotlin OAuth2/OIDC Authorization Server
- management-ui -> React/TypeScript admin UI
- helm-charts -> Helm chart deployment assets
- e2e -> Playwright end-to-end tests driving management-ui against auth-server
- website -> Astro public project website served at https://vauthenticator.com

The glossaries are listed in `CONTEXT-MAP.md`; decisions spanning several projects live in the root `docs/adr/`.

Always use the closest AGENTS.md file. Root instructions apply only when a subtree does not have its own guide or when the work is explicitly cross-project.

Project boundaries:

- `platform`: `vauthenticator-platform-parent` (build conventions and managed versions) and `vauthenticator-platform` (the shared mechanisms: Document storage, then cache, events, i18n, extensions); it holds mechanisms, never a server's concepts, and never imports a server. The root `pom.xml` aggregates it with `auth-server`, so a platform change is built and tested against the servers in the same change (`docs/adr/0001`)
- `auth-server`: backend authorization server, local auth-server assets, and auth-server docs
- `management-ui`: standalone React admin UI, nginx local serving config, and UI docs
- `helm-charts`: Kubernetes/Helm packaging and chart documentation
- `e2e`: end-to-end test suite, its compose stack and e2e-only configuration; it builds `auth-server` and `management-ui` from the working tree; a fix in their sources surfaced by e2e is a cross-project change and must be called out
- `website`: static public website pages, layouts and website docs; its content describes the other projects but never changes their code

Never mix changes across projects unless explicitly requested. If a contract must change across projects, state that coupling clearly and update the related docs in the same change.

Release notes live only in the root `changelog/<version>.md`, which `.github/workflows/release.yml` publishes as the GitHub release body: one file per version, covering every project, with an `## :warning: Upgrade notes` section for database migrations and changed configuration or chart values. Inner projects never get a `changelog` folder; they carry documentation only, in their README files and `docs/` folders. Until the release, record an upgrade note in the PR description and its issue.

Do not edit generated outputs by hand, including `auth-server/target`, `management-ui/dist`, `auth-server/dist`, `website/dist`, dependency directories, or packaged Helm chart archives.
