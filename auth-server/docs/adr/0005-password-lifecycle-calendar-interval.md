# Password lifecycle rules run on a calendar interval, not on password age

A Password Lifecycle Rule fires every time its Evaluation Interval elapses. The first interval is counted from the
rule's (re-)registration, and later intervals from its Last Evaluation (the moment it last fired). The interval
ignores when the account's password was last changed: a voluntary password change, or a reset forced by the rule
itself, does not restart it. We chose a plain recurring admin timer over password expiry. It is predictable and
independent of the password flows, and it is what the feature is meant to do: "act on this account every N days".

**Considered options:**
- Password expiry, counting the interval from the account's last password change and restarting it on every change
  (for example via `ChangePasswordEvent`/`ResetPasswordEvent`). Rejected: it couples the rule to every password
  flow, and it is not the intended behaviour. The name "Password Lifecycle" suggests this option, which is why the
  decision is recorded here.

**Consequences:**
- With a `PASSWORD_RESET` rule of `P10D` registered on day 0, a user who changes their password on day 9 is still
  forced to reset on day 10. If the user ignores a forced reset for a few days, the next forced reset comes that
  much sooner after they finally do it.
- `ACCOUNT_LOCK` is the exception to the recurring cadence. It fires once, and the rule is then deleted.
- Re-registering a rule (`PUT`) restarts its interval from the moment of registration.
- A rule fires on the first job run after its interval has elapsed, so the effective precision is the
  `password.password-life-cycle.cron` period.
