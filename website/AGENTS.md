# Project website

This is the public informational VAuthenticator website, built with Astro and served at https://vauthenticator.com from S3 + CloudFront. Keep website code and content here; do not change the authorization server, management UI, or Helm runtime code as part of website work.

- Pages are static. Markdown pages share `src/layouts/DocsLayout.astro`.
- The site is served from `/`; the base path stays configurable (`SITE_BASE`). Never hardcode it: use `sitePath` in Astro components and relative links (`../install/local/`) in Markdown pages.
- Keep capability claims grounded in the project code and docs. Label incomplete installation guidance clearly.
- `npm run build` and `npm run check:links` validate the production site.
- Do not edit `dist`, `.astro`, or `node_modules` by hand.
- The website is not published to GitHub Pages. `gh-pages` hosts only the Helm repository (`index.yaml` and chart downloads); never publish website files there.
