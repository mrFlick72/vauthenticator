# Project website

This is the public informational VAuthenticator website, built with Astro for GitHub Pages. Keep website code and content here; do not change the authorization server, management UI, or Helm runtime code as part of website work.

- Pages are static. Markdown pages share `src/layouts/DocsLayout.astro`.
- Prefix internal links with the configured `/vauthenticator/` base. Use `sitePath` in Astro components.
- Keep capability claims grounded in the project code and docs. Label incomplete installation guidance clearly.
- `npm run build` and `npm run check:links` validate the production site.
- Do not edit `dist`, `.astro`, or `node_modules` by hand.
- The GitHub Pages address also hosts the Helm repository. Publishing must preserve `index.yaml` and existing chart downloads on `gh-pages`.
