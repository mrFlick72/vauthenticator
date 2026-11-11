# Granular admin scopes can be granted by any client app writer

ADR 0017 (#365) makes the Full-Access Scope something only an Administrator can give to a client app
or take away from one. The granular `admin:*` scopes (`admin:account-writer`, `admin:key-editor`,
`admin:client-app-writer`, …) were left out of that rule, to be decided separately (issue #429). The
decision is **the Full-Access Scope is the only scope gated by who the caller is**. A caller with
`admin:client-app-writer` gives or takes away any other scope, `admin:*` scopes included, on any
client app that is not an Administrator. It does not need to hold the scopes it grants.

| Caller | Full-Access Scope, or any write to a client app that has it | Granular `admin:*` and other scopes on any other client app |
|---|---|---|
| Administrator | allowed | allowed |
| Client app with `admin:client-app-writer`, not an Administrator | `403`, nothing saved (ADR 0017) | allowed, including scopes it does not hold |

This mirrors the account side: only an Administrator gives or removes the Administrator Role
(ADR 0017), while `admin:account-writer` manages the other roles of accounts that are not
Administrators (ADR 0018). Administration is the one power that is gated by who the caller is. The
granular scopes are gated only by the scope the endpoint requires.

Nothing changes in the code. `ClientApplicationAdministration` keeps checking only the Full-Access
Scope. `ClientApplicationAdministrationTest` pins the decision: a caller that is not an
Administrator can give, take away and keep granular scopes, and change the secret of an app that
holds them, but not touch the Full-Access Scope, even alongside granular scopes.

**Considered options:**
- Only scopes the caller holds: a caller that is not an Administrator may give or take away only the
  `admin:*` scopes in its own token, and may not write an app holding scopes it lacks. Rejected: it
  makes delegated set-up of integrations depend on the writer collecting every scope it may ever
  hand out, and adds a second, scope-by-scope gate next to the administration one for a power that
  `admin:client-app-writer` is meant to carry.
- No `admin:*` scope at all for callers that are not Administrators. Rejected: it turns
  `admin:client-app-writer` into a scope that can register only login apps, and any integration
  set-up would need `admin:full-access`, a broader grant than the one it replaces.
- A configured list of the scopes such callers may grant. Rejected: one more security setting that
  operators must get right, while giving `admin:client-app-writer` only to trusted client apps
  already limits who can grant.

**Consequences:**
- `admin:client-app-writer` is close to Administrator power. Its holder can create a client app with
  any granular scope, or add them to its own app, and then reach every API those scopes open. What it
  cannot reach are Administrator accounts (ADR 0018) and Administrator client apps (ADR 0017).
  Operators give it only to client apps they trust that far.
- `admin:client-app-eraser` likewise deletes any client app that is not an Administrator, whatever
  scopes it holds.
- The management UI is unaffected: its users act as Administrators through the Administrator Role,
  and an account holder without it has no admin scopes to call these APIs with.
- A future scope that grants administration, the way `admin:full-access` does, must be added to the
  check in `ClientApplicationAdministration`, not left to this rule.
