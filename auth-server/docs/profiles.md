# Profile

VAuthenticator always stores its data in PostgreSQL and uses Redis for sessions, allowed origins, failed-login counters
and the optional caches. DynamoDB support was removed in 0.10 ([ADR 0012](adr/0012-postgresql-only-persistence-with-evict-on-write-caches.md));
there is no migration from a DynamoDB deployment.

The only Spring profile left is about key management. Use ```spring.profiles.active``` with

- ```kms``` to use AWS KMS
- omitting ```kms``` to use plain java security api

in case of plain java security implementation the follow configuration is required:


```yaml
key:
  master-key:
    storage:
      content:
        key : value
        key2 : value2
```

## Datasource

The PostgreSQL datasource is always required:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/
    username: postgres
    password: postgres
```

## Protected Roles

Roles that cannot be deleted because the system depends on them:

```yaml
vauthenticator:
  role:
    protected-from-delete: ROLE_USER,VAUTHENTICATOR_ADMIN
```

## Caches

Accounts, roles and client applications can be read through a Redis cache. Each cache is off unless enabled:

```yaml
vauthenticator:
  cache:
    account:
      enabled: true
      ttl: 1h
      name: account_cache
    role:
      enabled: true
      ttl: 1h
      name: role_cache
    client-application:
      enabled: true
      ttl: 1h
      name: client_application_cache
```

Every write to account data evicts the account cache, including a Failed-Login Lock being applied or released; deleting
a role or a group clears the whole account cache, because the delete also removes it from the accounts that hold it.
