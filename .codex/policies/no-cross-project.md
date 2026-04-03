# No Cross Project Changes Policy

This repository is a monorepo composed of independent projects:
- auth-server (Kotlin)
- management-ui (React)
- helm (Helm chart)

Rules:
- Never modify more than ONE project per change
- Do not update backend and UI in the same PR
- Do not update Helm when modifying application code
- If changes are required across projects, split into multiple commits
- Ask for confirmation before cross-project refactors

Project boundaries:

auth-server/
management-ui/
helm/

Allowed:
- auth-server only changes
- management-ui only changes
- helm only changes

Forbidden:
- auth-server + management-ui
- auth-server + helm
- management-ui + helm