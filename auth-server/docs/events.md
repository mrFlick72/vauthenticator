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
| client app | the client app the action is for; the System Client when there is none |
| timestamp | when the event was created |
| payload | what happened, per event type |
| request context | IP address, user agent and session reference of the request the event was created in; empty when there is no request, as for the password lifecycle job |

- **IP address**: `request.remoteAddr`. Deployments set `server.forward-headers-strategy: framework`, so behind an
  ingress this is the client address taken from `X-Forwarded-For`.
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
change-password events are forwarded too. A consumer that needs only Login Events filters on the payload class. An
event whose request has no resolvable client app is attributed to the System Client.

## Logger consumer

`LoggerEventConsumer` writes one log entry per event, including the request context. It is enabled with
`event.consumer.enable.logger-event-consumer: true` (Helm: `events.enableLoggerConsumer`, off by default). The
username (for a failed login, whatever was typed) and the user agent are client-controlled, so the logger escapes CR
and LF in the username, the user agent and the payload text, and truncates the user agent to 512 characters. The
encoded password carried by signup, password change and password reset events is written as `Password(*****)`.

IP address and user agent are personal data: whoever enables the logger, or adds a consumer, owns the retention of
what it writes.
