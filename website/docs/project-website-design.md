# Project website design

This document captures decisions from the grill-with-docs session and the first version of the website.

## Agreed scope

- Publish an informational VAuthenticator website through GitHub Pages.
- Focus the first version on prospective adopters. Discuss contributor sections later.
- Help visitors understand whether VAuthenticator fits their needs, then provide a clear path to getting started.
- Include a general introduction, dedicated feature pages, and installation guides.
- Prioritize local Docker installation for evaluation, followed by a separate Kubernetes installation guide using Helm.
- The local installation guide succeeds when the authorization server and management UI are running and the local tenant is provisioned.
- Feature pages explain each capability, when it is useful, and a small example, with links to detailed configuration documentation.
- Installation guides provide the complete steps needed to reach the documented working setup.
- Use a product-style homepage with a project introduction, architecture overview, feature highlights, and a prominent Get started action.
- Use documentation-style navigation on feature and installation pages.
- Publish the website at the existing GitHub Pages address, `https://mrflick72.github.io/vauthenticator`.
- Preserve the existing Helm chart repository index and chart downloads at that address so existing Helm repository commands continue to work.
- Organize feature pages into OAuth2 and OpenID Connect; accounts and password lifecycle; multi-factor authentication; administration through the management UI; and token customization and infrastructure options.
- Keep architecture and installation as separate site sections.
- Build the website with Astro in a dedicated root-level `website/` directory.
- Author feature pages and installation guides in Markdown.

## Delivery

- Open a GitHub issue containing the final scope and decisions when the grilling session concludes.
- Keep all website code on a dedicated branch before integration. The branch for this work is `feat/project-website`.

## First increment

- Deliver a small, usable Astro site: product homepage, architecture overview, five feature summaries, and local/Kubernetes installation landing pages.
- Use English initially, matching the existing project documentation.
- Use a midnight and electric-blue hero, with light content sections and a consistent light documentation layout. Keep the existing page structure, use restrained hover transitions, and respect reduced-motion preferences.
- Present installation pages as setup overviews linking to detailed project documentation. Describe the supported setup accurately without draft labels or promises of future walkthroughs.
- Build and check the site on pull requests. Publish website changes merged to `main`, with a manual publishing trigger as well.
- Keep the existing `gh-pages` publishing source and update only website-owned paths. Preserve Helm's `index.yaml` and packaged charts.
- Keep server, management UI, and chart implementations outside this increment.

## Follow-up increments

- Provide a verified local quick-start using released Docker images, covering a running server, management UI, and provisioned local tenant.
- Expand feature examples and Kubernetes installation instructions.
- Revisit contributor content after the adopter experience is useful.

## Existing setup evidence

The current local setup documentation describes infrastructure services, local hostnames, tenant provisioning, email verification, and MFA before signing into the management UI. The website installation guide needs a coherent path through these steps.

Reference: [Local tenant installer](../../auth-server/local/readme.md).
