# KeyEndPoint: migrate authorization from WebSecurityConfig to in-controller checks

`KeyEndPoint` (`/api/keys*`) was the last admin API enforcing authorization purely through
`WebSecurityConfig` `hasAnyAuthority(...)` request-matcher rules — the pattern ADR 0001 and ADR 0002
each rejected in favor of in-controller `permissionValidator.validate(principal, Scopes.from(...))`
calls. We migrated it to match, reusing the existing `admin:key-reader` (`GET /api/keys`) /
`admin:key-editor` (`POST /api/keys`, `POST /api/keys/rotate`, `DELETE /api/keys`) scopes — no new
scopes needed, and no change in who is authorized to do what.

**Considered options:**
- Keep the `WebSecurityConfig` rules alongside the new in-controller checks, as defense-in-depth for
  signing-key operations specifically, given how sensitive key material is — rejected. The primary
  motivation for this migration is testability: `KeyEndPoint` can now be unit-tested for
  authorization behavior (403 on missing scope, 2xx on `admin:full-access`) with a real
  `PermissionValidator` wired to a mocked `ClientApplicationRepository`, without a full Spring
  Security filter chain in play — matching how `ClientApplicationEndPointTest` already works. Two
  enforcement layers that must be kept in sync defeats that goal and reintroduces exactly the kind of
  drift VA-SEC-14 found (a filter-chain rule that silently stopped matching its own route).

**Consequences:**
- `KeyEndPoint`'s `WebSecurityConfig` style was the last holdout ADR 0002 called out as "the outlier
  now, not a live convention to keep extending" — every admin-scoped `/api/**` controller now
  enforces its own authorization in-controller, with `WebSecurityConfig` reduced to
  `permitAll()`/`authenticated()`.
- `/api/keys*` requests that pass authentication now rely solely on `KeyEndPoint`'s own
  `permissionValidator.validate(...)` calls for scope enforcement; there is no longer a
  filter-chain-level backstop specific to these routes beyond the generic `/api/**` `.authenticated()`.
