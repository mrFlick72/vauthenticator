# Events

## Abstract

VAuthenticator raises in-process events for what happens to accounts: logins (password and MFA checks), signup,
password change and reset, password lifecycle actions, the Failed-Login Lock and MFA device revocation. Consumers
react to them inside the auth-server, and the same pipeline is the base for auditing. A **Login Event** is defined in
[CONTEXT.md](../CONTEXT.md); the decisions about the request context are in
[ADR 0016](adr/0016-events-carry-request-context-captured-centrally-with-a-hashed-session-id.md).

## Pipeline

1. A domain flow dispatches a `VAuthenticatorEvent` through an `EventsDispatcher`. Spring Security authentication
   events (password login, `MfaSuccessEvent` / `MfaFailureEvent`, change-password events) are wrapped as a
   `VAuthenticatorAuthEvent` by `SpringEventEventsDispatcher`.
2. `SpringEventsCollector` receives every `VAuthenticatorEvent` and passes it to each `EventConsumer` whose
   `handleable` accepts it.
3. Consumers today: the logger, signup, welcome mail, verification mail, and the password history kept on reset and
   change.

Everything runs synchronously, on the thread that raised the event. Nothing is published outside the auth-server; a
broker or a store would be one more `EventConsumer`.

## What an event carries

| Field | Content |
|---|---|
| user | the account's username (for an authentication event, the username that was tried) |
| client app | the client app the action is for; none (`ClientAppId.empty()`) when a person acts without one; the System Client for actions the server starts itself, such as the password lifecycle job |
| timestamp | when the event was created |
| payload | what happened, per event type |
| request context | IP address, user agent and session reference of the request the event was created in; empty when there is no request, as for the password lifecycle job |

- **IP address**: `request.remoteAddr`. Deployments set `server.forward-headers-strategy: framework`, so behind an
  ingress this is the client address taken from `X-Forwarded-For` (its leftmost value). It is only as trustworthy as
  the ingress: one that appends to a client-sent `X-Forwarded-For` instead of overwriting it lets the client choose it.
- **User agent**: the raw `User-Agent` header, as sent by the client.
- **Session reference**: the SHA-256 hex of the session id, or empty when the request has no session. The session id
  itself is never put on an event.

### Session reference across a login

The session id changes when the password is proven (Spring Security session fixation protection), so one sign-in
carries two references:

| Event | Session reference |
|---|---|
| failed password attempts | the pre-login one, or none if no session existed yet |
| `AuthenticationSuccessEvent` | the pre-login one |
| `InteractiveAuthenticationSuccessEvent` | the post-login one |
| MFA passed or failed | the post-login one |

A consumer that groups a sign-in links the password check to the MFA check through
`InteractiveAuthenticationSuccessEvent`. Failed attempts before it can only be related by user, client app and time.

## Authentication events forwarded

`SpringEventEventsDispatcher` forwards every Spring authentication event, not only **Login Events**: each successful
password login appears twice (`AuthenticationSuccessEvent` and `InteractiveAuthenticationSuccessEvent`), and
change-password events are forwarded too. A consumer that needs only Login Events filters on the payload class.

Without a resolvable client app (request `client_id` parameter or the saved authorization request in the session),
only a username/password authentication is forwarded, with no client app: a direct `/login`, the MFA and
change-password events that carry the same token, and a logout (`LogoutSuccessEvent`, which ends the session a login
history pairs it with; a logout with a client app was already forwarded). Any other authentication without a client app, such as a bearer
token on an API call, is not forwarded, so API traffic is not recorded as logins.

## Logger consumer

`LoggerEventConsumer` writes one log entry per event, including the request context. It is enabled with
`event.consumer.enable.logger-event-consumer: true` (Helm: `events.enableLoggerConsumer`, off by default).

- The username (for a failed login, whatever was typed), the client id, the user agent and the IP address are
  client-controlled. The logger writes them through `escapedForLog()` (`extentions/LogExt.kt`): a backslash is doubled
  and control characters (CR, LF, NEL, ESC, ...), Unicode line and paragraph separators and format characters (bidi
  overrides, zero-width characters) are written as escapes. The user agent and the IP address are first truncated to
  512 characters, never in the middle of a character. `AccountUserDetailsService` logs the typed username the same way.
- An authentication payload is written as its event type, plus the exception type for a failure (for example
  `AuthenticationFailureBadCredentialsEvent(BadCredentialsException)`), never as its text, which holds the typed
  username and the raw session id.
- The encoded password carried by signup, password change and password reset events is written as `Password(*****)`.
- A missing client app is written as `none`.

IP address and user agent are personal data: whoever enables the logger, or adds a consumer, owns the retention of
what it writes.
