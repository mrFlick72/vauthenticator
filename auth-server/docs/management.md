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

It provides the default admin account and client apps, the signing keys, the default template of every email and SMS type in the document store (`email/templates/*.html`, `sms/templates/*.txt`), and the Default Page of every page (login, MFA, signup, password reset and change, email verification, errors) at the path the pages are served from (`static-asset/content/asset/[{asset-server.on-s3.bundle-version}/]*_bundle.js`). The default templates ship in the jar, under `src/main/resources/communication/default`; the Default Pages are built into the jar by Maven, under `pages/default`, and the setup fails if a page is missing (a jar built with `-Dskip.npm`).

Run it on a new tenant only: it saves the default templates, Default Pages and client apps again, replacing the templates an admin has edited, the pages an operator has replaced, and resetting the `admin` client secret and the management UI URIs. A tenant created before the templates were seeded by this endpoint gets a missing template by saving it through `PUT /api/email-template` or `PUT /api/sms-template` (or the management UI), not by running the setup again. Its Default Pages are uploaded to the document store by the operator; refreshing the pages of an existing tenant after an upgrade is tracked in #439.

*URI:* ```Post /actuator/tenant-setup```

*Request:* Empty request body

*Response Status:* ```204 NoContent```
