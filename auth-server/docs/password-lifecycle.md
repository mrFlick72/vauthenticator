# Password Lifecycle

## Abstract

VAuthenticator lets an administrator attach a password lifecycle rule to an account. Each rule has a TTL and an
action. A scheduled job evaluates the rules periodically and, once a rule's TTL has elapsed, applies its action to
the account:

- `PASSWORD_RESET`: sets the account mandatory action to `RESET_PASSWORD`, so the user is forced to reset the
  password at the next login
- `ACCOUNT_LOCK`: locks the account

The feature is available only with the `database` profile (PostgreSQL). Rules are stored in the
`PASSWORD_LIFECYCLE_RULES` table.

## How to

### Register a rule

*URI:* ```Put /api/admin/accounts/password/lifecycle```

*Scope:* ```admin:password-lifecycle-editor```

*Request Body:*

```json
{
  "userName": "user@email.com",
  "ttl": "P90D",
  "creationDate": "2026-10-06T00:00:00",
  "lastEvaluationDate": null,
  "action": "PASSWORD_RESET, ACCOUNT_LOCK"
}
```

- `ttl`: an ISO-8601 duration (for example `P90D` or `PT1H`). A number is read as seconds.
- `creationDate`: an ISO-8601 local date time. When `lastEvaluationDate` is `null`, the TTL is counted from this date.
- `lastEvaluationDate`: optional. When set, the TTL is counted from this date instead.

*Response Status:* ```204 No Content```

### Evaluation

`PasswordLifeCycleExecutorJob` reads all the rules in pages of 100 and runs the strategy that matches each rule's
action. A rule's action is applied when the current time is after `lastEvaluationDate` (or `creationDate`) plus `ttl`.
After the action is applied, `lastEvaluationDate` is set to the current time, so the action runs again only after
another full TTL. A rule whose TTL has not elapsed is left untouched.

The job schedule is a Spring cron expression with six fields
(`second minute hour day-of-month month day-of-week`). The default is every hour:

```yaml
password:
  password-life-cycle:
    cron: "0 0 * * * *"
```
