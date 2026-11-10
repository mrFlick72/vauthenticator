# Only an Administrator manages an Administrator account

ADR 0017 makes administration itself (the Administrator Role on an account, the Full-Access Scope on
a client app) something only an Administrator can change. Other admin operations on an account did
not look at who the account is: a caller with `admin:account-writer` could lock or disable every
Administrator account, and one with `admin:password-lifecycle-editor` could schedule a lock or a
forced password reset on them (issue #428). The rule is now **an Administrator account can be
managed only by an Administrator; any other account can be managed by a caller with the right
scope**, including a client app integrating machine to machine. Administrator is defined as in
ADR 0017.

## The rule, for every future admin operation

An **Administrator** is one of:
- a client app acting with the `admin:full-access` scope (machine to machine);
- an account holder with the `VAUTHENTICATOR_ADMIN` role acting through an access token (the
  management UI).

A browser session alone is never an Administrator.

| Caller | Administrator account | Account that is not an Administrator | Own account |
|---|---|---|---|
| Administrator | manages it | manages it | manages it |
| Client app with granular scopes only (`admin:account-writer`, `admin:password-lifecycle-editor`, …) | never: `403` for a single account, skipped with `207` in bulk | manages it, as far as its scopes allow | (a client app has no account) |
| Account holder who is not an Administrator | never | never | only through the self-service flows |

- **Only an Administrator manages an Administrator account**, and only an Administrator makes
  someone an Administrator or stops them being one (ADR 0017).
- **An Administrator manages every account.**
- **Granular scopes are for accounts that are not Administrators.** An M2M integration that must
  manage Administrator accounts needs `admin:full-access`.
- **Every account holder, Administrators included, manages their own account through the
  self-service flows:** password change, forgotten-password reset through the emailed link, email
  verification and MFA enrolment. These are authorized by the account holder's own session or
  ticket, not by an admin scope.

A new admin operation that acts on someone else's account must follow this table. Its use case calls
`AccountAdministration.checkManage` for a single account, or skips with `canManage` and answers `207`
in bulk. A new self-service flow must act only on the caller's own account.

Admin operations on someone else's account, with the decision for each:

| Operation | Scope | On an Administrator account, by a caller that is not an Administrator |
|---|---|---|
| `PUT /api/admin/accounts`: lock, unlock, enable, disable, mandatory action, roles | `admin:account-writer` | `403`, nothing saved |
| `PUT` / `DELETE /api/admin/accounts/{userName}/password/lifecycle/{action}` | `admin:password-lifecycle-editor` | `403`, nothing saved |
| `POST` / `DELETE /api/admin/accounts/password/lifecycle/{action}/bulk` | `admin:password-lifecycle-editor` | the account is skipped, the others are processed, `207` |
| `GET /api/admin/accounts/{email}/email`, `GET …/password/lifecycle` | reader scopes | allowed: they change nothing |

Left as they are, because they are the account holder's own flows or only send a link to the
account's own inbox: the forgotten-password reset through the emailed link (an Administrator who
forgets their password recovers it like anyone else), password change, email verification, MFA
enrolment, the reset-password, welcome and verification mails, and the password lifecycle job
applying rules that were registered under this rule.

`AccountAdministration.checkManage(stored)` requires an Administrator when the stored account is one.
`AccountUpdateAdminAction` and the single-account `PasswordLifeCycleRules.register`/`remove` call it;
`checkSave` keeps guarding administration itself for every writer. The bulk operations ask
`managesEveryAccount()` once and, when the caller is not an Administrator, load each matching account
and skip those it cannot manage (`canManage`). Both bulk operations now go through the matching
accounts page by page; the bulk removal deletes the rule of each account instead of one
`DELETE … ILIKE`, so the rule stays in the domain and not in a SQL filter. `AdministrationArchitectureTest`
also fails the build when a class writes password lifecycle rules without calling `AccountAdministration`;
`PasswordLifeCycleExecutorJob`, which only removes the rules of accounts that no longer exist, is exempt.
The `Administration` port gained `isAdministrator()`, with `requireAdministrator()` built on it.

A partly applied bulk request answers `207 Multi-Status` with the usual body, whose `matchedAccounts`
counts only the accounts processed. The response does not say which accounts were skipped, how many,
or why, so a caller that is not an Administrator learns no more than that something matched it could
not manage. One `WARN` log line records the operation, the action, the pattern, the client app and the
number of skipped accounts, never their usernames. The management UI shows the count with a generic
"some accounts were skipped" warning on `207`.

**Considered options:**
- A scope is enough, even on an Administrator account. Rejected: a helpdesk or M2M client could lock
  every Administrator out, the lockout ADR 0017 already refused for removing the role.
- Rejecting a whole bulk request that matches an Administrator account with `403`, as ADR 0017 does
  for single writes. Rejected: a bulk rule for `*@company.com` is a routine job for such a caller, and
  the Administrators in the pattern are not what it is asking to change.
- A `200` with a header or a body field describing the skipped accounts. Rejected: the details would
  tell a caller that is not an Administrator which accounts are Administrators. `207` is the only
  common 2xx that means "partly done"; `206` is for ranges and `202` for work not yet done. Unlike
  WebDAV, the body is not a per-item `multistatus` document.
- Skipping Administrator accounts in SQL (`NOT EXISTS` on the role) inside both repository queries.
  Rejected: it is faster for very large patterns, but it puts a business rule about who may administer
  into persistence, which ADR 0017 refused for repository decorators.

**Consequences:**
- A non-Administrator bulk operation reads every matching account, one query each; an Administrator's
  does not. The bulk removal issues one delete per matching account where it issued one statement, and
  no longer removes rules left by accounts that no longer exist; accounts are not deleted today, and the
  lifecycle job removes such rules when it meets them.
- Unlocking an Administrator account, including after the Failed-Login Lock, needs an Administrator;
  that lock also lifts itself when it expires.
- `207` is a 2xx, so clients that check only for success keep working; one that cares about skipped
  accounts checks for exactly `207`.
- Callers that can be Administrators keep their behaviour: the management UI acts with the Administrator
  Role, so its users never get `207` or this `403` unless their account lacks the role.
