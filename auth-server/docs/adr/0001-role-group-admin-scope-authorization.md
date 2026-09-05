# Role/Group admin APIs: in-controller scope checks, 3-tier reader/writer/eraser

`RoleEndPoint` and `GroupEndPoint` (VA-SEC-01) had no authorization at all — any authenticated
principal, any scope, could read, write, or delete roles and groups. We fixed this with
in-controller `permissionValidator.validate(principal, Scopes.from(...))` calls, matching
`ClientApplicationEndPoint`'s pattern, using new 3-tier scopes: `admin:role-{reader,writer,eraser}`
and `admin:group-{reader,writer,eraser}`. `GroupEndPoint`'s combined associate/de-associate
endpoint (`PUT /api/groups/{groupId}/roles`) requires `group-writer` for both directions, since it
mutates group membership rather than deleting the group itself; `group-eraser` is reserved for
`DELETE /api/groups/{groupId}`.

**Considered options:**
- `WebSecurityConfig` `hasAnyAuthority` request-matcher rules, matching `KeyEndPoint` — rejected;
  we want the scope check to live next to the code it protects, not in a separate filter-chain
  config that's easy to add an endpoint without touching.
- 2-tier reader/writer-covers-delete, matching `KeyEndPoint` — rejected in favor of 3-tier to match
  `ClientApplicationEndPoint`'s finer granularity, since write and delete don't carry the same
  blast radius for roles/groups (delete affects every account already assigned that role/group).

**Consequences:**
- A prior version of this ADR picked the opposite on both points (2-tier, `WebSecurityConfig`) and
  was deleted before anything was implemented against it — this is the version that shipped.
- `AdminApiAccountEndPoint` (also part of VA-SEC-01) is not covered here and still has no
  authorization; that's a separate decision.
- A future admin domain should pick one of the two enforcement mechanisms and one of the two
  tiering conventions deliberately, not by copying whichever controller was open last.
