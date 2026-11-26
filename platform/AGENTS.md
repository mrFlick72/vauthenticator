# VAuthenticator Platform Agent Guide

## Purpose

The platform is the starting point of every VAuthenticator application: the mechanisms more than one server needs, in one jar,
plus the parent POM every server inherits. The decision and its trade-offs are in the root `docs/adr/0001`; the
vocabulary is in `CONTEXT.md`.

## Modules

- `vauthenticator-platform-parent`: parent of `vauthenticator-platform` and of every server. It decides how a module is built (Kotlin baseline,
  `kotlin-maven-plugin` with the Spring allopen plugin, `src/main/kotlin` and `src/test/kotlin`) and which versions it
  uses (`dependencyManagement`: the AWS SDK BOM, MockK, ArchUnit, `vauthenticator-platform`). It never decides what a server contains:
  web, security, Thymeleaf, Node and `spring-boot-maven-plugin` stay in the server's own POM.
- `vauthenticator-platform`: the shared mechanisms, one package per feature under `com.vauthenticator.platform`:
  - `document`: Document storage (`domain`: `Document`, `DocumentRepository`; `adapter`: `S3DocumentRepository`,
    `FileSystemDocumentRepository`; `autoconfigure`: `DocumentAutoConfiguration`, `DocumentProperties`)
  - `cache`: `CacheOperation` and its Redis implementation `RedisCacheOperation` (one hash per key, a TTL per region,
    `evictAll` by region prefix), `CacheContentConverter`. Spring Data Redis is `optional`: the server brings it and its
    `RedisTemplate`. The regions themselves (which data is cached, `vauthenticator.cache.<region>.*`) and the
    evict-on-write rule stay in the server (auth-server `CacheConfig`, ADR 0012)
  - `events`: the in-process event pipeline: the abstract `VAuthenticatorEvent` (principal, client id, timestamp,
    payload, `RequestContext`) every server's events extend, the `EventsDispatcher`/`EventsCollector`/`EventConsumer`
    contracts generic on the event type, `PublishingEventsDispatcher` (publishes as a Spring application event), the consumer loop
    `ConsumingEventsCollector`, `LoggingEventConsumer` (switched on by `event.consumer.enable.logger-event-consumer`;
    escapes every client-controlled value, logs a `SensitivePayload` and a Spring Security authentication event as their
    type only), `EventConsumerConfig` and `RequestContext` (IP, user agent, SHA-256 session reference, never the session
    id: auth-server ADR 0016). A server defines its event subclasses on top of `VAuthenticatorEvent` (typed constructor
    arguments, plain-string state), binds the contracts, the dispatcher and the logger to it and puts `@EventListener`
    on its collector's `accept` override, so it receives `VAuthenticatorEvent`s and no other application event
    (auth-server `SpringEventsCollector`). A payload that must never be written, such as a password, implements `SensitivePayload`
  - `i18n`: `I18nMessageRepository` reads the messages and errors of a scope (a key prefix) from the server's
    `i18n/messages_<lang>.properties` bundle, falling back to `i18n/messages.properties`; `I18nMessages`. The scopes,
    the bundles and how messages reach a page stay in the server (auth-server `I18nScope`, `I18nMessageInjector`)
  - `extensions`: server-agnostic Kotlin extensions: `toSha256`, `escapedForLog` (the log-forging guard every server
    must use for client-controlled text), `expirationTimeStampInSecondFromNow`, the Base64 `encoder`/`decoder`. Extensions
    on a server's types stay in that server (the auth server's `extentions` package)

## Conventions For Changes

- The platform holds mechanisms, never a server's concepts. A package that mixes the two is split when it moves: the server
  keeps its own types (events, client apps, accounts), the platform gets the generic part. The platform never imports a server.
- Every Document belongs to one server; the platform knows no Document Type of its own: the server names its types (a plain
  name, the first segment of the storage key `<type>/<path>`) and owns any cache on top of the repository.
- Each feature is wired by its own auto-configuration, listed in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, and configured by its own
  properties. A missing or wrong setting stops the startup with a message naming the key
  (`DocumentStorageConfigurationException` for documents), never a generic missing-bean error. A server or test that
  defines its own bean turns the platform's off (`@ConditionalOnMissingBean`).
- Infrastructure clients come from the server: Document storage uses the server's `S3Client` bean. Integration SDKs are
  `optional` dependencies, loaded only by the code path that needs them, so a server that does not use them does not
  carry them.
- Keep the `document.*` keys stable: `engine` (`s3` or `file-system`), `bucket-name`, `fs-base-path`. They are rendered
  by the Helm chart; renaming one is a breaking configuration change with an upgrade note.

## Build And Test

From the repo root (the Maven wrapper is there):

- `./mvnw install` builds the platform and every server
- `./mvnw test -pl platform/platform -am` for the platform only

`S3DocumentRepositoryTest` expects LocalStack on `localhost:4566` and `RedisCacheOperationTest` Redis on `localhost:6379`, like the auth-server tests: CI provides it as a job
service; locally use `auth-server/src/test/resources/docker-compose.yml`. `DocumentAutoConfigurationTest` needs no
infrastructure.
