# The session client-app fallback fails closed, and reset-password redeems only reset-password tickets

ADR 0007 made `PermissionValidator` the single authorization path for `/api/**`: a bearer JWT first, otherwise the client application read from the session (`HttpSession.oauth2ClientId()`: the saved `/oauth2/authorize` request or the `clientId` attribute set by the login page). The anonymous browser APIs (signup, reset-password challenge) and the browser MFA APIs depend on that fallback. For issue [#393](https://github.com/mrFlick72/vauthenticator/issues/393) we made it safe to depend on, and settled how `PUT /api/reset-password/{ticket}`, the last `/api` handler without a `PermissionValidator` call, is authorized.

- **Fail closed.** When the session has no client app, or the one it names does not exist, the validator throws `ClientApplicationNotResolvedException`, mapped to `403` by `ExceptionAdviceController`. Previously an unknown client app was authorized (VA-SEC-07) and a missing one returned `500`.
- **The reset-password ticket is the authorization.** `PUT /api/reset-password-challenge` is where the client app's `reset-password` scope is checked; the ticket it issues is the proof of that check. `SendResetPasswordMailChallenge` marks its tickets with `purpose=resetPassword` in the ticket context, and `ResetAccountPassword` redeems only tickets with that purpose; any other ticket is an `InvalidTicketException`, like an expired or used one. `PUT /api/reset-password/{ticket}` therefore has no `PermissionValidator` call.

**Considered options:**
- Check `reset-password` again when the ticket is redeemed, against the session's or the ticket's client app — rejected: it repeats the challenge's check, needs the client app stored in the ticket, and fails for links opened in another browser or after the session expired. It also does not close the actual gap: tickets of every flow share one store, so without a purpose check a ticket issued for another flow (MFA enrolment returns its ticket to the caller) could be redeemed to reset a password.
- Make all of `/api/**` `permitAll()` now that every authorizing handler validates in the controller — rejected: `/api/**` `.authenticated()` plus explicit `permitAll()` for the anonymous paths keeps a forgotten validator call from turning a new handler public.

**Consequences:**
- Session-based callers whose session has no resolvable client app get `403` instead of being authorized (unknown client app) or `500` (no client app).
- Reset-password tickets issued before this change have no purpose and are rejected; the user requests a new link. Reset tickets are short-lived, so no transition rule was added.
- A new ticket-based flow must set its own purpose and check it when redeeming, so its tickets cannot be used by other flows and vice versa.
