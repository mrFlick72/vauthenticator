# Build an incremental VAuthenticator project website with Astro and GitHub Pages

## Goal

Give prospective adopters a public website that explains VAuthenticator, helps them evaluate its capabilities, and provides a path to installation. Ship a basic first draft and improve it incrementally.

## Decisions from the grill-with-docs session

- Audience: prospective adopters first; contributor content comes later.
- Visitor journey: understand the project and assess fit, then get started.
- Homepage: product-style introduction, architecture overview, feature highlights, and a prominent Get started link.
- Inner pages: documentation-style navigation, authored in Markdown.
- Technology and ownership: Astro in a dedicated root-level `website/` directory, independent of the authorization server, admin UI, and Helm charts.
- Feature pages: OAuth2/OIDC; accounts and password lifecycle; MFA; administration through the management UI; token customization and infrastructure options.
- Feature depth: explain each capability, its use cases, and a small example; link to detailed configuration documentation.
- Installation: local Docker first, Kubernetes with Helm second.
- Local setup finish line: a working authorization server, management UI, and provisioned local tenant.
- Desired eventual quick-start: released Docker images rather than source builds; a new verified Compose example is needed.
- Hosting: reuse https://mrflick72.github.io/vauthenticator/ and preserve the Helm chart repository index and downloads at that URL.
- All website code must be developed on a dedicated branch before integration. First draft branch: `feat/project-website`.

## Increment 1: basic website draft

- [ ] Product homepage, architecture overview, and responsive navigation.
- [ ] Five feature pages with concise, accurate summaries.
- [ ] Local and Kubernetes installation landing pages that link to existing instructions and clearly indicate their draft status.
- [ ] Independent Astro build and local development instructions.
- [ ] GitHub Actions build validation and publishing to the existing `gh-pages` branch without deleting Helm artifacts.
- [ ] Verify production build, internal links/assets under `/vauthenticator/`, and desktop/mobile layouts.

English and a responsive light theme are initial implementation defaults. Increment 1 does not claim to supply a completed Docker quick-start or exhaustive feature documentation.

## Follow-up increments

- [ ] Add and validate the image-based Docker quick-start through local tenant provisioning.
- [ ] Expand feature examples and Kubernetes installation guidance.
- [ ] Discuss contributor-focused sections.

## Acceptance criteria

The first draft can be served as a static site under `/vauthenticator/`, provides useful introductory content, and has no dead internal navigation. Publishing preserves existing Helm repository functionality. No runtime code changes to `auth-server`, `management-ui`, or Helm templates are required for this increment.
