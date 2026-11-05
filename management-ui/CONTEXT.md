# VAuthenticator Management UI

The admin interface through which an administrator manages client applications, keys, roles, accounts and templates on the authorization server.

## Language

### Signing in

**Session**:
The admin's logged-in session on the authorization server, the same session the auth-server glossary means. The admin interface only observes it; it ends on the authorization server, through logout or expiry.
_Avoid_: login, SSO session (in admin-facing text)

**Expired Session**:
A **Session** that has ended. Continuing needs the admin to log in again with their credentials.
_Avoid_: logged out (when the admin did not log out)

**Expired Access**:
The admin interface's permission to call the management api has run out while the **Session** is still alive. Continuing needs no credentials: the interface renews its access from the living session.
_Avoid_: expired session, session timeout
