# PostgreSQL is the only persistence, with opt-in Redis caches kept correct by evicting on every write

For issue [#402](https://github.com/mrFlick72/vauthenticator/issues/402) DynamoDB support is removed and PostgreSQL becomes unconditional: the `database` profile no longer exists, and neither do the `!database` branches that kept OAuth2 authorizations in Redis. Maintaining two persistence adapters per repository doubled the work for every feature (the Failed-Login Lock of ADR-0011 was already PostgreSQL-only), and PostgreSQL can be deployed in any environment. VAuthenticator is in alpha, so this is a breaking release with no Dynamo-to-PostgreSQL migration: existing Dynamo deployments redeploy from scratch.

The Redis read-through caches that used to wrap only the Dynamo repositories (account, role, client application) are kept and moved onto the JDBC repositories, because a cache in front of the account, role and client application reads is considered essential for production. They stay opt-in (`vauthenticator.cache.<account|role|client-application>.enabled`, default `false`).

- **Every write to account data evicts the account cache.** Not only `AccountRepository.save`: any code that updates the account tables directly, such as the conditional `UPDATE ACCOUNT` that applies and releases a Failed-Login Lock, must evict the account's entry too. Otherwise a lock lands in PostgreSQL while logins keep reading the cached unlocked account until the TTL expires.
- **Deleting a role or group clears the whole account cache region.** The deletion cascades into account role and group memberships in the database, below the cache. Role and group deletes are rare admin actions, so emptying the region is cheaper to get right than finding and evicting the affected accounts.
- The cache lives in Redis and is shared by all replicas, so an eviction is seen by every instance.

**Considered options:**
- Delete the caches and read PostgreSQL directly: rejected, caching is wanted in production.
- Cache roles and client applications but never accounts, keeping lock, expiry and verification state out of the cache: rejected in favour of caching accounts too, at the cost of the eviction rule above.
- A short TTL that tolerates staleness: rejected, a stale cached account defeats an account lock.
- Evicting only the accounts that held a deleted role or group: rejected, it spreads knowledge of account tables and cache keys into the role and group adapters.
- Refusing to delete a role or group still assigned to accounts: rejected, it changes admin behaviour outside this decision.

**Consequences:**
- Deployments must set a datasource; the Helm chart renders it unconditionally and `profiles` no longer lists `dynamo` or `database`.
- The `vauthenticator.dynamo-db.*` properties disappear. The cache settings move to `vauthenticator.cache.*`, and the list of **Protected Roles** moves to `vauthenticator.role.protected-from-delete`.
- Any new code that writes account tables without going through the cached repository has to evict, and needs a test that a cached account reflects the write.
- Making the remaining AWS integrations (SNS, S3, KMS, Lambda clients and credentials) optional is a separate decision, tracked in [#404](https://github.com/mrFlick72/vauthenticator/issues/404).
