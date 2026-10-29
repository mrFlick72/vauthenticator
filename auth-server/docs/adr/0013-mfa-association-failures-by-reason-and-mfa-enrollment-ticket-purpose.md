# MFA association failures are reported by reason, and MFA enrollment tickets carry a purpose

For issue [#339](https://github.com/mrFlick72/vauthenticator/issues/339) we made `POST /api/mfa/associate` tell its callers why an association failed. Before, an invalid ticket was an unhandled `InvalidTicketException` (`500`) and a wrong code was an `MfaException`, an `AuthenticationException` that reached Spring Security's entry point instead of the controller, so a JWT caller could not tell "try the code again" from "enroll again" from "already done". The same change settles which tickets the endpoint redeems, extending ADR 0008 to MFA enrollment.

- **Three reasons, one exception.** Every association failure is an `MfaAssociationFailureException` carrying an `MfaAssociationFailureReason`, mapped in `MfaEnrolmentAssociationEndPoint` to a `{ "reason": ... }` body, as the revocation refusals of ADR 0009 are:
  - `WRONG_CODE` → `422`: the ticket stays valid, the caller retries with another code.
  - `INVALID_TICKET` → `410`: unknown, expired, used, issued to another account, without the `mfaEnrollment` purpose, or its device was revoked meanwhile; the caller enrolls the device again.
  - `ALREADY_ASSOCIATED` → `409`: the device already counts as a second factor; the code is not checked and the ticket is consumed.
  The reason is the contract; the status is a hint. The exception is a `RuntimeException`, not an `AuthenticationException`, so a wrong code no longer reaches the security entry point.
- **Associated channels cannot be enrolled again.** `POST /api/mfa/enrollment` on a channel whose device is already associated is refused with `409` `ALREADY_ASSOCIATED`, instead of sending a code for a ticket that could never succeed. A channel whose device is only enrolled can still be enrolled again (same device, new code).
- **The ticket is the authorization, and it must be an MFA enrollment ticket for the caller.** Account-holder enrollment tickets carry `purpose=mfaEnrollment`; `/api/mfa/associate` redeems only those, and only for the account they were issued to. A ticket of another account is `INVALID_TICKET` and is left in place, so a stranger cannot consume someone's pending enrollment and the response does not confirm that the ticket exists.
- **Email verification keeps its own path.** Email verification reuses MFA enrollment to issue its link ticket, on the account's email device with self-association. It is exempt from the re-enrollment refusal (a verification resend for an already associated email device must still work), its tickets now carry `purpose=emailVerification` so the association endpoint refuses them, and it still tolerates `ALREADY_ASSOCIATED` when the link is redeemed.

**Considered options:**
- One status for every association failure, with only the body reason differing — rejected: the issue asked for statuses a caller can act on without parsing, and a single status keeps the old 401/500 ambiguity visible in logs and proxies.
- More `InvalidTicketCause` values on `InvalidTicketException` — rejected: a wrong code is not a ticket problem, and `InvalidTicketException` is shared with reset-password and email verification, which do not need these reasons.
- Refuse re-enrollment for every caller of `MfaMethodsEnrollment.enroll` — rejected: it would break the verification resend for accounts whose email device is already associated.

**Consequences:**
- MFA enrollment tickets issued before this change have no purpose and are rejected as `INVALID_TICKET`; the caller enrolls again. They are short-lived, so no transition rule was added.
- Redeeming a verification link does not check the `emailVerification` purpose yet, so links sent before this change keep working. Enforcing it is a follow-up once those links have expired.
- `InvalidTicketCause` loses its only non-default use and is removed.
