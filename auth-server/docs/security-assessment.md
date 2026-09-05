# VAuthenticator — Security Assessment & Remediation Tracker

> Status: **IN PROGRESS — dependency CVEs patched (2026-08-28), design/config findings still open.**
> Date of review: 2026-06-09
> Reviewed branch: `helm-chart-revamp`
> Scope: `auth-server` (Spring Boot 4.0.6 / Kotlin 2.3.21 / Java 25), plus `helm-charts` runtime config.
> Spec basis: `auth-server/AGENTS.md`.

This is a working document. Each finding has a stable ID (`VA-SEC-NN`), a severity, concrete
file references, impact, a recommended fix, and a checkbox to track remediation. Pick up future
work from the **Suggested remediation order** section at the bottom.

At the time of this review the framework stack was current, so the findings below are about
**design and configuration gaps**, not stale-dependency CVEs. That assumption has since been
disproven for at least one dependency: see the **2026-08-28 update** under VA-SEC-12. GitHub's
Dependabot alerts (`https://github.com/mrFlick72/vauthenticator/security`) are now the source of
truth for dependency-CVE status; this doc covers design/config only, plus cross-references where
a dependency CVE sharpens a design finding.

**2026-08-28** — issue [#349](https://github.com/mrFlick72/vauthenticator/issues/349) / PR
[#350](https://github.com/mrFlick72/vauthenticator/pull/350) patched the following Dependabot
alerts across the monorepo (not itemized as VA-SEC findings since they're plain version bumps,
not design gaps): `com.hubspot.jinjava:jinjava` 2.8.1→2.8.3 (CVE-2026-25526, critical — see
VA-SEC-12), `org.bouncycastle:bcpkix-jdk18on` 1.81→1.84 (medium — see VA-SEC-04), plus 14 npm
alerts in `auth-server/src/main/frontend` and 8 in `management-ui/src` (webpack, @babel/core,
fast-uri, lodash, minimatch, nanoid, picomatch, postcss, serialize-javascript, yaml, ajv,
react-router/react-router-dom).

---

## Methodology — what was reviewed

- Both Spring Security filter chains: `config/WebSecurityConfig.kt`, `config/AuthorizationServerConfig.kt`.
- Every `/api/**` controller and its authorization story (scope rules vs. in-controller `PermissionValidator`).
- OAuth2/OIDC client adaptation and token settings: `oauth2/registeredclient/ClientAppRegisteredClientRepository.kt`.
- MFA enrollment / challenge / verification: `mfa/**`.
- Key management crypto (non-KMS path): `keys/adapter/java/**`.
- Password reset / change / policy: `password/**`, `ticket/**`.
- Runtime configuration: `src/main/resources/application.yml`, `local/application.yml`,
  `helm-charts/charts/vauthenticator/templates/vauthenticator.yaml`.
- Verified absence of method security: no `@EnableMethodSecurity` / `@PreAuthorize` / `@Secured` anywhere.

---

## Finding summary

| ID | Severity | Title |
|----|----------|-------|
| VA-SEC-01 | **Critical** | Admin account / role / group APIs have no authorization (privilege escalation) |
| VA-SEC-02 | **Critical** | Actuator endpoints unauthenticated (tenant-setup, database-clean-up, shutdown, env, heapdump) |
| VA-SEC-03 | **High** | No anti-automation: login + MFA OTP + reset are brute-forceable |
| VA-SEC-04 | **High** | Signing keys & MFA secrets wrapped with AES/ECB, unauthenticated (non-KMS) |
| VA-SEC-05 | **High** | OAuth2 consent always bypassed; refresh tokens reused, not rotated |
| VA-SEC-06 | **Medium** | Signup trusts caller-supplied `authorities`/`groups` |
| VA-SEC-07 | **Medium** | Fail-open authorization in the session/client validation path |
| VA-SEC-08 | **Medium** | Clickjacking protection disabled globally; no CSP |
| VA-SEC-09 | **Medium** | Weak/insecure shipped defaults (Security TRACE logging, minSize=1, committed sample key, `println`) |
| VA-SEC-10 | **Low** | CORS fragility (credentialed + `allowedHeaders:*` + `*` default origin) |
| VA-SEC-11 | **Low** | Missing cookie/transport hardening (Secure/SameSite/channel) |
| VA-SEC-12 | **Low** | Jinjava email templates = privileged code execution surface (document & gate) |
| VA-SEC-13 | **Low** | Possible flag-mapping bug: `accountNonLocked` decoded from `credentialsNonExpired` |
| VA-SEC-14 | **Medium** | `WebSecurityConfig` scope rule for email-template read never matches; falls back to authenticated-only |

---

## CRITICAL

### VA-SEC-01 — Admin account / role / group APIs have no authorization
- [ ] Fixed
- [ ] Tests added

**2026-09-05 update — Role/Group portion fixed, Account portion still open:** `RoleEndPoint` and
`GroupEndPoint` now call `permissionValidator.validate(principal, Scopes.from(...))` with new
`admin:role-{reader,writer,eraser}` / `admin:group-{reader,writer,eraser}` scopes (3-tier, matching
`ClientApplicationEndPoint`'s convention; see issue
[#362](https://github.com/mrFlick72/vauthenticator/issues/362)). `AdminApiAccountEndPoint`
(`PUT /api/admin/accounts`, `GET /api/admin/accounts/{email}/email`) is unchanged and still has no
authorization check — this finding stays open until that's addressed separately.

**Where:**
- `account/api/AdminApiAccountEndPoint.kt:14` (`GET /api/admin/accounts/{email}/email`), `:21` (`PUT /api/admin/accounts`)
- `account/domain/AccountUpdateAdminAction.kt:13` (writes `authorities`, `enabled`, `accountNonLocked` from request body)
- `role/api/RoleEndPoint.kt:15` (`GET /api/roles`), `:19` (`PUT /api/roles`), `:25` (`DELETE`)
- `role/api/GroupEndPoint.kt:11` (`GET /api/groups`), `:17`, `:23`, `:29` (`PUT /api/groups/{id}/roles`)
- `config/WebSecurityConfig.kt:141` — catch-all `.requestMatchers("/api/**").authenticated()`

**Problem:** These controllers neither call `permissionValidator` nor have a scope rule in
`WebSecurityConfig`. There is no method security in the codebase. So they resolve to the
catch-all rule requiring only a **valid token** — any authenticated principal qualifies,
including a normal end-user's `openid` access token.

**Impact (vertical privilege escalation / account takeover):**
- `PUT /api/admin/accounts` with `{"email":"me@x","accountLocked":false,"enabled":true,"authorities":["VAUTHENTICATOR_ADMIN","admin:full-access"]}`
  grants admin to any account.
- `GET /api/admin/accounts/{email}/email` enumerates accounts and discloses their authorities + lock/enabled state.
- `PUT /api/roles` / `PUT /api/groups/{id}/roles` define and bind authorities.
- `GET /api/roles` / `GET /api/groups` disclose the full role/group and permission structure to any
  authenticated principal — lower severity than the writes above, but still no authorization check.

**Why it's a likely oversight:** the correct pattern already exists — `ClientApplicationEndPoint`,
`MfaEnrolmentAssociationEndPoint`, `EMailVerificationEndPoint`, `AccountEndPoint` (signup),
`RestPasswordEndPoint` all call `permissionValidator.validate(principal, Scopes.from(...))`.
These three were missed.

**Fix:**
- Add explicit scope/role rules in `WebSecurityConfig` for `/api/admin/**`, `/api/roles/**`,
  `/api/groups/**` (e.g. require `ADMIN_FULL_ACCESS` or a dedicated account-admin scope + `VAUTHENTICATOR_ADMIN`).
- Add in-controller `permissionValidator.validate(...)` to match the rest of the codebase.
- Consider `@EnableMethodSecurity` + `@PreAuthorize` as defense-in-depth so a future missed
  route fails closed rather than open.

---

### VA-SEC-02 — Actuator endpoints unauthenticated
- [ ] Fixed
- [ ] Tests added

**Where:**
- `local/application.yml:70-78` and **production** `helm-charts/charts/vauthenticator/templates/vauthenticator.yaml:149-162`:
  `management.endpoints.web.exposure.include: "*"`, `endpoint.shutdown.enabled: true`, `health.show-details: ALWAYS`, on a separate management port (9091 local / 8081 prod).
- `management/init/TenantSetUpEndPoint.kt` (`@Endpoint(id="tenant-setup")`, `@WriteOperation`)
- `management/cleanup/DatabaseTtlEntryCleanJobEndPoint.kt` (`@Endpoint(id="database-clean-up")`)
- No `SecurityFilterChain` covers the management context. Both app chains use `securityMatcher(...)`
  scoped to app paths, so nothing matches `/actuator/**`; a separate management port is not covered
  by the app's `HttpSecurity`.

**Impact:**
- `POST /actuator/tenant-setup` re-runs account/client/key bootstrap → can recreate default admin creds/clients.
- `POST /actuator/database-clean-up`, `POST /actuator/shutdown` → data loss / DoS.
- `/actuator/env`, `/actuator/heapdump`, `/actuator/configprops` → leak master key, DB/AWS creds.

Only mitigated if the management port is strictly network-isolated; the shipped config gives no in-app defense.

**Fix:**
- Dedicated `SecurityFilterChain` using `EndpointRequest.toAnyEndpoint()` requiring an admin authority on the management context.
- Restrict `exposure.include` to `health,info,prometheus`.
- Authenticate/authorize `tenant-setup` / `database-clean-up` / `shutdown` explicitly (or convert the
  state-changing ones to authenticated admin APIs rather than open actuator write operations).

---

## HIGH

### VA-SEC-03 — No anti-automation (brute force)
- [ ] Fixed
- [ ] Tests added

**Where:**
- No failed-login counter / lockout anywhere (`accountNonLocked` is only set by signup/verify/admin, never by a failure handler).
- MFA OTP: `mfa/domain/OtpMfa.kt:37-66` (`TaimosOtpMfa`), config `local/application.yml:18-21` → 6 digits, **600s** TTL, 600000 ms variance window, no attempt cap.
- Failure handlers `config/WebSecurityConfig.kt:218-230` just redirect; they don't throttle.

**Impact:** A first-factor holder can brute-force the 6-digit OTP within its 10-minute validity.
Password login is likewise unthrottled.

**Fix:** per-account + per-IP throttling and lockout on `/login`, `/mfa-challenge`, `/api/mfa/*`,
and reset endpoints; shorten OTP TTL to 30–120s; cap OTP verification attempts per challenge.

---

### VA-SEC-04 — AES/ECB unauthenticated key wrapping (non-KMS)
- [ ] Fixed
- [ ] Tests added

**Where:** `keys/adapter/java/JavaSecurityCryptographicOperations.kt:39` and `:48` —
`Cipher.getInstance("AES")` resolves to `AES/ECB/PKCS5Padding`. Wraps RSA private signing keys and TOTP secrets at rest.

**Impact:** ECB is deterministic, has no IV and no integrity (CWE-327). Tamperable, leaks structure.

**Fix:** `AES/GCM/NoPadding` with a random 96-bit IV stored with the ciphertext (and a versioned
format for migration), or use the KMS profile in production. Plan a re-encryption/migration path for existing stored keys.

**2026-08-28 update:** `org.bouncycastle:bcpkix-jdk18on` was bumped 1.81→1.84 in PR #350
(Dependabot #54, broken/risky cryptographic algorithm). That's an orthogonal library-level CVE
fix in a dependency that sits in this same non-KMS crypto path — it does not fix the AES/ECB
design issue documented above, which is still open.

---

### VA-SEC-05 — Consent always bypassed; refresh tokens reused
- [ ] Fixed
- [ ] Tests added

**Where:** `oauth2/registeredclient/ClientAppRegisteredClientRepository.kt:86`
(`requireAuthorizationConsent(false)` hardcoded — stored `autoApprove` ignored) and `:94`
(`reuseRefreshTokens(true)`).

**Impact:** No consent screen is ever shown, even for public/third-party clients. A leaked refresh
token stays replayable for its whole lifetime.

**Fix:** honor `autoApprove` (default to requiring consent for non-first-party clients); enable
refresh-token rotation, especially for public PKCE clients.

---

## MEDIUM

### VA-SEC-06 — Signup trusts caller-supplied authorities
- [ ] Fixed
- [ ] Tests added

**Where:** `account/api/AccountEndPoint.kt:98` (`SignUpAccountConverter` maps
`representation.authorities`/`groups` onto the new account). Requires `admin:signup` scope.

**Impact:** A client with only `admin:signup` can mint an account carrying `VAUTHENTICATOR_ADMIN` /
`admin:full-access`, breaking separation between "create users" and "grant admin".

**Reference pattern:** the self-service `PUT /api/accounts` path preserves stored authorities correctly
(`account/domain/SaveAccount.kt:18`). Signup should do likewise — strip or whitelist authorities on creation.

---

### VA-SEC-07 — Fail-open authorization in session/client path
- [ ] Fixed
- [ ] Tests added

**Where:** `role/domain/PermissionValidator.kt:48` —
`clientApplicationRepository.findOne(clientAppId)?.let { if (!hasEnoughScopes) throw }`. If the client
doesn't resolve, the `?.let` is skipped and **no exception is thrown**, so authorization passes.
(Also note the `//todo to add scope validation for the admin:full-access scope` at `:16`.)

**Fix:** fail closed — throw when the client cannot be loaded.

---

### VA-SEC-08 — Clickjacking disabled globally; no CSP
- [ ] Fixed
- [ ] Tests added

**Where:** `config/WebSecurityConfig.kt:65` and `config/AuthorizationServerConfig.kt:148` —
`http.headers { it.frameOptions { it.disable() } }`. Login, MFA, consent, change-password pages are fully framable.

**Fix:** use `SAMEORIGIN` (sufficient for the OIDC `check_session_iframe`) instead of `disable()`;
add a `Content-Security-Policy` with `frame-ancestors` for the HTML pages.

---

### VA-SEC-09 — Weak/insecure shipped defaults
- [ ] Fixed
- [ ] Tests added

**Where:**
- `src/main/resources/application.yml:5-7` — `org.springframework.security.*: TRACE` as a packaged default (can log tokens/credentials).
- `local/application.yml:88-91` — password policy `minSize: 1`, `minSpecialSymbol: 1`.
- `local/application.yml:27` — sample master key committed in repo.
- `oauth2/clientapp/api/ClientApplicationEndPoint.kt:90-91` and `oidc/sessionmanagement/AuthorizeSessionState.kt:49` — `println(...)` diagnostics.

**Fix:** sane logging default (INFO/WARN), stronger baseline password policy, document the sample key
as dev-only (and ensure prod uses KMS or a secret-managed key), replace `println` with a logger.

---

### VA-SEC-14 — Email-template read scope rule never matches its endpoint
- [ ] Fixed
- [ ] Tests added

**Where:**
- `config/WebSecurityConfig.kt:115-120` — `.requestMatchers(HttpMethod.GET, "/api/email-template").hasAnyAuthority(Scope.MAIL_TEMPLATE_READER.content, ...)`.
- `communication/api/EMailEndPoint.kt:15` — the actual mapping is `GET /api/email-template/{emailType}`.

**Problem:** `PathPatternRequestMatcher` (used throughout `WebSecurityConfig`, see the import at
`:31`) requires an exact match for a pattern with no wildcard. `/api/email-template` never matches
a request to `/api/email-template/{emailType}`, so this rule is dead code — the GET falls through
to the catch-all `.requestMatchers("/api/**").authenticated()` instead. The sibling `PUT
/api/email-template` rule (`:122-127`) is unaffected, since that mapping has no path variable and
matches exactly.

**Impact:** Any authenticated principal (any valid token, any scope) can read any email template
via `GET /api/email-template/{emailType}`, not only callers holding `MAIL_TEMPLATE_READER` /
`ADMIN_FULL_ACCESS` / the admin role as intended. Information disclosure of internal communication
templates, not a privilege-escalation path.

**Fix:** change the matcher to `"/api/email-template/**"` (or `/api/email-template/{emailType}`)
so it actually covers the mapped path; add a test asserting a token with only `openid` gets 403 on
this route.

---

## LOW / hygiene

### VA-SEC-10 — CORS fragility
- [ ] Fixed

`web/cors/DynamicCorsConfigurationSource.kt` sets `allowCredentials = true` with `allowedHeaders = ["*"]`;
clients are persisted with `AllowedOrigins.empty()` = `setOf("*")` (`oauth2/clientapp/domain/ClientApplication.kt:253`).
Exact-match resolution means it isn't directly exploitable today, but "*" in a credentialed config is a
footgun. Store explicit origins; avoid "*".

### VA-SEC-11 — Cookie/transport hardening
- [ ] Fixed

No `Secure`/`SameSite` cookie config or `requiresChannel().requiresSecure()` in code; relies on the
ingress. Set session cookie flags explicitly and consider HSTS.

### VA-SEC-12 — Jinjava templates are privileged code
- [ ] Documented / gated

`communication/adapter/JinJavaTemplateResolver.kt:8` renders admin-written email templates with account
data in context. Treat `admin:email-template-writer` as a code-execution-equivalent grant; document and
tightly control it (and consider Jinjava sandbox settings).

**2026-08-28 update — severity re-scored from Low to reflect a live CVE, not just a hypothetical
design risk:** Dependabot alert #23 (critical, CVE-2026-25526) found that Jinjava's sandbox itself
was bypassable via `ForTag` property access and `ObjectMapper`-based class instantiation, allowing
arbitrary Java class instantiation and filesystem reads regardless of app-level gating. This was
independent confirmation that the sandbox this finding relies on ("consider Jinjava sandbox
settings") could not be trusted below version 2.8.3. Fixed in PR #350 by bumping
`com.hubspot.jinjava:jinjava` 2.8.1→2.8.3. The **design-level ask in this finding — documenting and
tightly gating `admin:email-template-writer`, and revisiting Jinjava sandbox configuration now that
the underlying sandbox bug is patched — is still open.**

### VA-SEC-13 — Possible flag-mapping bug
- [ ] Verified / fixed

`account/domain/Account.kt:148` decodes `accountNonLocked = it["credentialsNonExpired"] as Boolean`.
Verify this path; conflating the two flags could mis-lock/unlock accounts.

---

## Suggested remediation order

1. **VA-SEC-01** and **VA-SEC-02** — drop-everything; reachable with no special preconditions, both look like oversights.
2. **VA-SEC-03** (brute force) and **VA-SEC-05** (consent/refresh) — core IdP correctness.
3. **VA-SEC-04** (AES-GCM) — needs a migration plan for already-stored keys.
4. **VA-SEC-06**, **VA-SEC-07**, **VA-SEC-14** — small, high-value access-control fixes.
5. **VA-SEC-08**, **VA-SEC-09** — headers + defaults hardening.
6. Low/hygiene batch (**VA-SEC-10..13**).

## Notes / caveats for whoever picks this up

- No prod application.yml exists in-repo besides the Helm configmap; confirm real deployments don't override the
  insecure actuator exposure before downgrading VA-SEC-02 severity.
- Verify network isolation of the management port (8081/9091) in the actual cluster — it changes blast radius, not the fix.
- When fixing VA-SEC-01, mirror the existing `permissionValidator` pattern and add characterization tests in
  `src/test/kotlin/com/vauthenticator/server` next to the matching domain.
- Tests: `./mvnw test`. Security-related tests live under the same domain package names.
- Dependency CVEs (Jinjava, BouncyCastle, npm packages in both `auth-server/src/main/frontend`
  and `management-ui/src`) are tracked via GitHub Dependabot alerts, not as VA-SEC findings in
  this doc — see issue [#349](https://github.com/mrFlick72/vauthenticator/issues/349) / PR
  [#350](https://github.com/mrFlick72/vauthenticator/pull/350) for the 2026-08-28 sweep. Re-check
  `https://github.com/mrFlick72/vauthenticator/security/dependabot` periodically; VA-SEC-12 shows
  a dependency CVE can invalidate this doc's "framework stack is current" assumption for a
  specific library even when the overall stack is recent.
