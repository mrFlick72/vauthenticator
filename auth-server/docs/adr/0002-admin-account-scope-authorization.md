# AdminApiAccountEndPoint: in-controller scope checks, flat 2-tier reader/writer

`AdminApiAccountEndPoint` (VA-SEC-01, the portion left open by ADR 0001) had no authorization at
all — any authenticated principal, any scope, could read any account's authorities/lock state and
overwrite them. We fixed this with in-controller `permissionValidator.validate(principal,
Scopes.from(...))` calls, matching `RoleEndPoint`/`GroupEndPoint`/`ClientApplicationEndPoint`, using
two new scopes: `admin:account-reader` (`GET /api/admin/accounts/{email}/email`) and
`admin:account-writer` (`PUT /api/admin/accounts`).

**Considered options:**
- `WebSecurityConfig` `hasAnyAuthority` request-matcher rules, matching `KeyEndPoint` — rejected for
  the same reason as ADR 0001: the check should live next to the code it protects.
- 3-tier reader/writer/eraser, matching `ClientApplicationEndPoint`/Role/Group — rejected because
  this controller has no delete endpoint; there's nothing for an eraser tier to gate.
- Splitting `admin:account-writer` into a plain tier (lock/enable) and a stricter tier gating changes
  to `authorities` (since the write endpoint can grant `VAUTHENTICATOR_ADMIN`) — rejected for now.
  Deferred as its own finding: **VA-SEC-15** / issue #365. Doing it here would have required
  `AccountUpdateAdminAction` to diff the incoming `authorities` against the stored account, which is
  real logic beyond a scope-annotation change, and would have blurred this PR's scope.

**Consequences:**
- ADR 0001's open question ("ADR 0001 → a future admin domain should pick one enforcement mechanism
  and one tiering convention deliberately") is now resolved the same way twice (Role/Group, Account):
  in-controller `permissionValidator`. `KeyEndPoint`'s `WebSecurityConfig` style is the outlier now,
  not a live convention to keep extending.
- Until VA-SEC-15 is fixed, anyone holding `admin:account-writer` can still self-escalate to
  `VAUTHENTICATOR_ADMIN` via the `authorities` field — this ADR closes VA-SEC-01, not VA-SEC-15.
