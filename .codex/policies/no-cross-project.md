# No Cross Project Changes Policy

This repository is a monorepo composed of independent projects:
- auth-server (Kotlin)
- management-ui (React)
- helm-charts (Helm)

## Hard Rule

The agent MUST modify files in ONLY ONE project directory per task.

A project is identified by its root folder:

- auth-server/**
- management-ui/**
- helm-charts/**

The agent MUST NOT:
- edit files across multiple project folders
- create commits touching multiple projects
- refactor shared code affecting multiple projects
- update Helm when changing application code
- update UI when changing backend

## Enforcement

If a requested change requires modifying multiple projects:
- STOP
- Explain why multiple projects are required
- Ask user confirmation
- Propose split into multiple changes

## Allowed Examples

✔ Only:
- auth-server/**

✔ Only:
- management-ui/**

✔ Only:
- helm-charts/**

## Forbidden Examples

✘ auth-server/** + management-ui/**  
✘ auth-server/** + helm-charts/**  
✘ management-ui/** + helm-charts/**

## Exception

Documentation files are allowed across projects:
- README.md
- docs/**
- images/**