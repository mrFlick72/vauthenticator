# VAuthenticator website

An incremental, static Astro website for people considering VAuthenticator. This project is independent of the authorization server and management UI.

## Run locally

Use Node.js 22.12 or newer. From this directory:

```sh
npm ci
npm run dev
```

Open http://localhost:4321/.

## Build and preview

```sh
npm run build
npm run check:links
npm run preview
```

The build generates static files in `dist/`, and the link check verifies that internal URLs work under the `/vauthenticator/` GitHub Pages base path. The repository workflow publishes successful builds while preserving the Helm repository files already hosted on `gh-pages`.

## Content

- `src/pages/index.astro`: product homepage.
- `src/pages/features/`: Markdown feature summaries.
- `src/pages/install/`: installation overviews linking to project setup and configuration documentation.
- `src/layouts/`: shared website and documentation layouts.
- `src/styles/global.css`: responsive styling.

Installation pages describe the setup path and link to detailed instructions for the authorization server, management UI, and Helm charts.
