# Password Lifecycle

## Abstract

VAuthenticator lets an administrator attach password lifecycle rules to an account. Each rule has an action and an
interval. A scheduled job evaluates the rules periodically and, once a rule's interval has elapsed, applies its action
to the account:

- `PASSWORD_RESET`: sets the account mandatory action to `RESET_PASSWORD`, so the user is forced to reset the
  password at the next login. It is **recurring**: it fires again every interval.
- `ACCOUNT_LOCK`: locks the account. It is **one-shot**: after it fires, the rule is deleted.

An account has at most one rule per action. The domain vocabulary is defined in [CONTEXT.md](../CONTEXT.md).

The feature is available only with the `database` profile (PostgreSQL). Rules are stored in the
`PASSWORD_LIFECYCLE_RULES` table, whose primary key is `(user_name, action)`.

### The interval is calendar based

The interval is a recurring timer: it is counted from the rule's registration for the first firing, and from the last
firing afterwards. It is **not** the password's age. A password change, voluntary or forced by the rule itself, does
not restart it. For example, with a `PASSWORD_RESET` rule of `P10D` registered on day 0, a user who changes the
password on day 9 is still forced to reset it on day 10. See [ADR 0005](adr/0005-password-lifecycle-calendar-interval.md).

## How to

All the endpoints require the scope ```admin:password-lifecycle-editor```.

### Register or replace a rule

*URI:* ```PUT /api/admin/accounts/{userName}/password/lifecycle/{action}```

`action` is `PASSWORD_RESET` or `ACCOUNT_LOCK`.

*Request Body:*

```json
{
  "interval": "P3M"
}
```

- `interval`: a positive ISO-8601 duration in the `PnYnMnWnDTnHnMnS` form, with any combination of designators,
  for example `P3M`, `P1Y`, `P1Y6M`, `P2W`, `P90D`, `PT12H` or `P1MT12H`. Years, months, weeks and days are calendar
  based: `P3M` registered on 7 October is due on 7 January, and a month added to 31 January ends on the last day of
  February. Hours, minutes and seconds are exact. Numbers, zero, negative parts, and intervals that go beyond the
  supported date range are rejected with `400 Bad Request`.
- The server sets the rule's creation date to now and clears its last evaluation. Registering a rule again for the same
  account and action replaces it and restarts its interval.

*Response Status:*

- ```204 No Content```: the rule is registered
- ```400 Bad Request```: the interval or the action is not valid
- ```404 Not Found```: the account does not exist

### Read the rules of an account

*URI:* ```GET /api/admin/accounts/{userName}/password/lifecycle```

*Response Body:*

```json
[
  {
    "action": "PASSWORD_RESET",
    "interval": "P3M",
    "creationDate": "2026-10-07T10:00",
    "lastEvaluationDate": null,
    "nextEvaluationDate": "2027-01-07T10:00"
  }
]
```

`interval` is returned exactly as it was registered. An account with no rules returns `[]`.

### Remove a rule

*URI:* ```DELETE /api/admin/accounts/{userName}/password/lifecycle/{action}```

*Response Status:* ```204 No Content```, whether the rule existed or not

### Evaluation

`PasswordLifeCycleExecutorJob` reads all the rules in pages of 100 and runs the strategy that matches each rule's
action. A rule fires when the current time is after its last evaluation (or its creation date, before the first
firing) plus its interval. A rule whose interval has not elapsed is left untouched. When a rule fires:

1. The action is applied only if it is not already in effect: an account already locked, or already required to reset
   its password, is left unchanged.
2. If the account changed, a `PasswordLifeCycleActionAppliedEvent` is published. Since no client application is
   involved, the event's client id is the system client `vauthenticator-system`.
3. A `PASSWORD_RESET` rule records the firing time as its last evaluation. An `ACCOUNT_LOCK` rule is deleted.

A failing rule does not stop the job: the error is logged and the remaining rules are evaluated. A rule whose account
no longer exists is deleted.

The job schedule is a Spring cron expression with six fields
(`second minute hour day-of-month month day-of-week`). The default is every hour:

```yaml
password:
  password-life-cycle:
    cron: "0 0 * * * *"
```

A rule fires on the first job run after its interval has elapsed, so the effective precision of an interval is the
cron period. The next interval is counted from the actual firing, so the schedule drifts by up to one cron period at
each firing, and a monthly rule that once lands on a shorter month keeps the earlier day of the month (31 January,
28 February, 28 March, ...).

### Known limitations

- The job has no cluster coordination: with more than one replica, every replica evaluates the rules and a firing
  can be applied and published more than once.
- Account deletion does not exist yet. When it is introduced, it is expected to publish an account deleted event that
  the password domain consumes to delete the account's rules. Until then, rules of missing accounts are deleted by
  the job.
