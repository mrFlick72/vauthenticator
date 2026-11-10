# Only an Administrator changes administration, checked in the use cases

`admin:account-writer` could give any account the `VAUTHENTICATOR_ADMIN` role through
`PUT /api/admin/accounts`, and `admin:client-app-writer` could give any client app the
`admin:full-access` scope or take over the default `admin` app (VA-SEC-15, issue #365). The rule
is now **only an Administrator can make someone an Administrator, or stop them being one**. An
Administrator is a JWT with the `admin:full-access` scope or the `VAUTHENTICATOR_ADMIN` role
(`isAdministrator()`, next to `hasEnoughScopes`, which uses it). A browser session never counts,
even one whose account has the role, matching ADR 0007's JWT-only admin APIs. The rule covers:

- adding or removing the Administrator Role on an account;
- any write (save, secret reset, delete) to a client app that has, or would get, the Full-Access
  Scope. Changing the secret, grant types or redirect URIs of the `admin` app takes it over as
  surely as granting the scope does.

A write that breaks the rule is rejected whole with `403`, with no partial save.

The rule lives in two domain services, `AccountAdministration` (`checkSave(stored, requested)`,
`checkCreate(requested)`) and `ClientApplicationAdministration` (`checkSave(stored, requested)`,
`checkDelete(stored)`). They compare the stored entity with the one being written and, only when
administration is touched, ask the `Administration` domain port to require an Administrator. Writes
that don't touch administration pass with or without a caller. Account creation looks only at the
new account, because comparing it with an existing one would let a public signup find out whether
an email belongs to an Administrator. The port keeps Spring Security out of the domain: its adapter,
`SecurityContextAdministration`, reads the caller from the Spring Security context.

Every use case that writes accounts or client apps calls the check just before writing, including
the ones that never change administration today (self-service save, password change and reset, the
password lifecycle job, email verification), so one that starts changing it later is still covered.
The client-app delete moved out of the controller into `DeleteClientApplication` for the same reason.
`AdministrationArchitectureTest` (ArchUnit) makes the check impossible to forget: the build fails
when a class calls `AccountRepository.save`/`create` without calling `AccountAdministration`, or
`ClientApplicationRepository.save`/`delete` without calling `ClientApplicationAdministration`.
Repository adapters, which only delegate, and the tenant setup jobs are exempt.

Tenant setup is the one writer that creates Administrators with no caller. Its `SignUpUse`, private
to `TenantInitConfig`, is wired on `TenantSetUpAdministration`, a file-private `Administration` that
always allows. It exists only in the wiring and no request can reach it, so it is not a system
Administrator that code able to set the security context could claim. The architecture test also
fails if any `Administration` other than `SecurityContextAdministration` and
`TenantSetUpAdministration` appears in main code. `Administration` is deliberately not a
`fun interface`, so an implementation cannot hide in a lambda the test cannot see.

**Considered options:**
- A stricter scope (for example `admin:account-authorities-writer`) for any role change, as
  proposed in #365. Rejected: it gates every role, while the escalation is specifically
  administration, and it does not stop a holder of the new scope from granting administration.
- Checking the token only for `admin:full-access`. Rejected: management UI users act with an
  `openid email profile` token and are Administrators only through the role, so they could no
  longer grant administration.
- Gating only the grant. Rejected: removing administration can lock a tenant out of
  administration just as granting it escalates.
- Applying the allowed part of the request and silently keeping the stored administration.
  Rejected: a `204` for a request that was partly ignored hides bugs and probing.
- Decorators around `AccountRepository` and `ClientApplicationRepository`, so every write passes
  the check at runtime without the writer calling anything. Tried first and rejected: a business
  rule about who may administer belongs in the domain services, not in persistence, and the
  decorators needed undecorated `tenantSetUp*` beans hidden behind `defaultCandidate = false`. The
  architecture test gives the same "cannot be forgotten" guarantee at build time.
- Calling the check only from the use cases that can change administration today. Rejected: a
  writer outside that list that later starts changing authorities would pass unnoticed.
- One application service as the only writer of accounts, called instead of the repository.
  Rejected: it changes every writer as well and still needs the architecture test to stop a direct
  repository call.
- Putting the check in `PermissionValidator`. Rejected for the enforcement: it sees only the
  caller and the required scopes, not the stored and requested entities. It keeps the definition
  (`isAdministrator()`).

**Consequences:**
- The adapter depends on the thread-bound Spring Security context. A writer that touches
  administration outside a request (a new job) fails closed with `403` unless it is deliberately
  wired like tenant setup.
- `StoreClientApplication.store` now always reads the stored app, also when it stores a new
  secret, and the client-app delete reads it before deleting.
- Signup (`POST /api/accounts`, JWT or browser session with `admin:signup`) is checked too, so a
  non-Administrator signup carrying `VAUTHENTICATOR_ADMIN` now gets `403`. This closes only the
  Administrator part of VA-SEC-06: signup still takes other caller-supplied roles and groups.
- The `VAUTHENTICATOR_ADMIN` role must stay in `vauthenticator.role.protected-from-delete`, as it
  does by default (local, e2e, Helm `values.yaml`): deleting a role is not an account write.
- Only administration itself is gated here. Further admin operations and scope grants are reviewed
  in #428 and #429.
- The domain services and use cases are tested with a stand-in `Administration`
  (`AdministrationFixture`). Which callers count as Administrators is tested on
  `SecurityContextAdministration`, setting `SecurityContextHolder` directly, because standalone
  MockMvc tests with `.principal(...)` do not fill the security context.
  `e2e/tests/accounts.spec.ts` (a management UI Administrator adding and removing
  `VAUTHENTICATOR_ADMIN`) covers the role-based Administrator path end to end.
- This is the named exception to "`/api/**` authorization lives in the controllers" (ADR 0007):
  scope checks stay in the controllers, while the administration rule lives in the domain services
  the use cases call.
