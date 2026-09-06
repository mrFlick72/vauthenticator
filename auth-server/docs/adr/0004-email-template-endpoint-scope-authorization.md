# EMailEndPoint: delete the dead WebSecurityConfig rule instead of patching its pattern

`EMailEndPoint` (`/api/email-template*`, VA-SEC-14) had a `WebSecurityConfig` `GET` rule that
matched the literal pattern `/api/email-template` — but the actual mapping is
`/api/email-template/{emailType}`, and `PathPatternRequestMatcher` requires an exact match. The
rule silently never fired, so any authenticated principal, any scope, could read any email
template. We fixed this with in-controller `permissionValidator.validate(principal,
Scopes.from(...))` calls in both `getMailTemplate()` and `saveMailTemplate()`, reusing the existing
`admin:email-template-reader` / `admin:email-template-writer` scopes, and deleted both
`WebSecurityConfig` rules for `/api/email-template*` outright.

**Considered options:**
- Fix the `WebSecurityConfig` GET pattern to `/api/email-template/**` and keep both filter-chain
  rules as the enforcement mechanism — rejected. That's the "obvious" fix for the immediate bug, but
  it leaves the underlying problem in place: a `PathPatternRequestMatcher` pattern that must be kept
  in exact sync with the controller's `@GetMapping` path, with no test failing when it drifts (the
  PUT rule already got this right only because its mapping happens to have no path variable). This
  is the same class of bug ADR 0003 called out for `KeyEndPoint`, and the same testability
  motivation applies: `EMailEndPoint`'s authorization is now unit-testable without a full Spring
  Security filter chain.
- Keep the (fixed) filter-chain rule as defense-in-depth alongside the new in-controller checks —
  rejected for consistency with ADR 0003's `KeyEndPoint` decision; two enforcement layers that must
  independently stay correct is what produced VA-SEC-14 in the first place.

**Consequences:**
- `/api/email-template*` now relies solely on `EMailEndPoint`'s own `permissionValidator.validate(...)`
  calls for scope enforcement, with `WebSecurityConfig` reduced to the generic `/api/**`
  `.authenticated()` catch-all, same as `KeyEndPoint` (ADR 0003), `ClientApplicationEndPoint`,
  `RoleEndPoint`/`GroupEndPoint` (ADR 0001), and `AdminApiAccountEndPoint` (ADR 0002).
- This closes VA-SEC-14: `GET /api/email-template/{emailType}` now requires
  `admin:email-template-reader` (or `admin:full-access`), not just a valid token.
