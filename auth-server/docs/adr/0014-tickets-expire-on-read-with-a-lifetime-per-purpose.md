# Tickets expire on read, with a lifetime per purpose

A ticket is the one-time proof behind MFA enrollment, reset-password links and email-verification links: it is deleted when it is redeemed. Its expiry used to depend on `POST /actuator/database-clean-up` deleting the row, which nothing runs on a schedule, so an unredeemed ticket stayed redeemable after its TTL. All tickets also shared one 5-minute TTL, which is too short for links read in an inbox. For issue [#339](https://github.com/mrFlick72/vauthenticator/issues/339) we made expiry part of reading a ticket and gave each purpose its own lifetime.

- **Expired means missing.** `JdbcTicketRepository.loadFor` returns a ticket only while `ttl` (its expiry, in epoch seconds) is not in the past. Every flow already treats a missing ticket as invalid, expired or used, so no flow needed its own check. The clean-up endpoint now only removes rows that can no longer be redeemed.
- **A lifetime per purpose**, chosen by `TicketFeatures.ttlFor` from the ticket's purpose and set by Spring properties (Spring durations), exposed in the Helm chart as `application.ticket.ttl.*`:
  - `ticket.ttl.mfa-enrollment`, default `5m`: the code is sent at once and typed in the same session.
  - `ticket.ttl.reset-password`, default `15m`: the link has to arrive and be opened.
  - `ticket.ttl.email-verification`, default `24h`: signups are verified hours later.
  A ticket without a known purpose gets the MFA enrollment lifetime, the shortest.

**Considered options:**
- Schedule the clean-up job instead — rejected: a ticket would still be redeemable between its expiry and the next run, and correctness would depend on a job's schedule.
- Keep one TTL for every ticket — rejected: any single value is either too long for MFA enrollment or too short for links read in an inbox.

**Consequences:**
- Tickets already stored keep the expiry computed when they were issued (5 minutes), so links sent before this change stop working once they are older than that.
- Changing a TTL property affects only tickets issued afterwards.
