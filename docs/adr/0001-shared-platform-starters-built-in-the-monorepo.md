# Shared platform starters are built in the monorepo, not published

VAuthenticator is moving towards several servers: the authorization server, then a communication
server and an identity server. Capabilities that more than one server needs, starting with Document
storage, leave `auth-server` for a new `platform/` project. The decision is **`platform/` holds a
parent POM (`vauthenticator-parent`) and one Spring Boot starter per capability
(`vauthenticator-<capability>-starter`), built together with the servers by a root aggregator
`pom.xml` and released with the same version**. Nothing is published to a Maven registry.

The parent decides how a module is built and which versions it uses: the Kotlin baseline, the
compiler plugins, Jacoco, and managed versions such as the AWS SDK BOM. It never decides what a
server contains. Framework dependencies a server needs (web, security, Thymeleaf, Node for its
pages) stay in that server's own POM, so a starter or another server does not inherit them.

A starter brings storage mechanisms, never shared data: every Document belongs to exactly one
server, and servers never read each other's Documents. A starter knows no Document Type of its own.
The server that uses it names the types and owns their caches.

**Considered options:**
- One "framework" jar bundling every shared capability. Rejected: the servers need different
  capabilities, and every server would carry every capability's dependencies, the AWS SDK included.
- A separately versioned platform published to GitHub Packages or Maven Central, in this repo or in
  its own. Rejected for now: GitHub Packages needs a token even to read public packages, so every
  contributor and CI job would need credentials to build, and Maven Central release work is not worth
  it for a platform whose only consumers live in this repo. A change to a starter is also tested
  against every server in the same PR. Revisit when something outside this repo needs a starter.

**Consequences:** CI, e2e and release build from the root aggregator, and the release workflow sets
the version there. A server's build needs the `platform/` sources next to it, so the servers cannot
be built from their own folder alone without running `install` on the platform first.
