# Management

Custom Actuator Endpoint

## Clean Database Entry with TTL

Actuator Clean Database

*URI:* ```Post /actuator/database-clean-up```

*Request:* Empty request body

*Response Status:* ```204 NoContent```

An expired ticket can already not be redeemed (ADR ```docs/adr/0014-tickets-expire-on-read-with-a-lifetime-per-purpose.md```); this endpoint only removes the rows.

## Tenant Setup

Actuator to create a local tenant configuration setup it is the successor of the py script in the local-environment folder 

It provides the default admin account and client apps, the signing keys, and the default template of every email and SMS type in the document store (`email/templates/*.html`, `sms/templates/*.txt`). The default templates ship in the jar, under `src/main/resources/communication/default`.

Run it on a new tenant only: it saves the default templates and client apps again, replacing the templates an admin has edited and resetting the `admin` client secret and the management UI URIs. A tenant created before the templates were seeded by this endpoint gets a missing template by saving it through `PUT /api/email-template` or `PUT /api/sms-template` (or the management UI), not by running the setup again.

*URI:* ```Post /actuator/tenant-setup```

*Request:* Empty request body

*Response Status:* ```204 NoContent```
