# The platform, the shared base of every server, is one jar built in the monorepo

VAuthenticator is moving towards several servers: the authorization server, then a communication
server and an identity server. The mechanisms more than one server needs leave `auth-server` for
the **platform** (`platform/`), the starting point of every VAuthenticator application: Document storage first,
then cache, events, i18n and extensions. The decision is **`platform/` holds a parent POM
(`vauthenticator-platform-parent`) and one jar (`vauthenticator-platform`) with every shared mechanism, built together with the servers
by a root aggregator `pom.xml` and released with the same version**. Nothing is published to a
Maven registry.

The platform holds mechanisms, never a server's concepts. Every Document belongs to exactly one server and
servers never read each other's Documents; the platform knows no Document Type of its own. Likewise the event
pipeline and the `VAuthenticatorEvent` every event extends move to the platform, with the account and the
client as plain strings, while each server keeps its own event subclasses and consumers, and the extensions that
work on a server's types (client apps, scopes, accounts) stay in that server. A package that mixes the
two is split when it moves.

The parent decides how a module is built and which versions it uses: the Kotlin baseline, the
compiler plugins and managed versions such as the AWS SDK BOM. It never decides what a server
contains: framework dependencies a server needs (web, security, Thymeleaf, Node for its pages) stay in
that server's own POM. Each platform mechanism is wired by its own auto-configuration and settings, and a
server replaces it by defining its own bean. Integration SDKs, such as the AWS S3 client, are optional
dependencies the server brings when it uses them.

The name is a plain description on purpose: a brand-like name such as "VAuth0" recalls Auth0 too closely.

**Considered options:**
- One Spring Boot starter per capability (`vauthenticator-<capability>-starter`). Rejected for now:
  the first step moves five mechanisms that every server will need, so a starter each adds modules
  and wiring without letting any server leave one out. Revisit when a server needs a mechanism the
  others do not.
- Moving the packages whole, with the auth-server types they use (`ClientAppId`, `Email`, the
  event subclasses and their payloads). Rejected: the platform would become most of the auth-server domain, and the next servers
  would inherit it.
- A separately versioned platform published to GitHub Packages or Maven Central, in this repo or in its
  own. Rejected for now: GitHub Packages needs a token even to read public packages, so every
  contributor and CI job would need credentials to build, and Maven Central release work is not worth
  it while every consumer lives in this repo. A change to the platform is also tested against every server in
  the same PR. Revisit when something outside this repo needs the platform.

**Consequences:** CI, e2e and release build from the root aggregator, and the release workflow sets
the version there. A server's build needs the `platform/` sources next to it, so a server cannot be built
from its own folder alone without running `install` on the platform first.
