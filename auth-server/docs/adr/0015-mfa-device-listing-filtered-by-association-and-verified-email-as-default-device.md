# MFA device listing is filtered by association, and a verified email is an associated default device

For issue [#343](https://github.com/mrFlick72/vauthenticator/issues/343) `GET /api/mfa/enrollment` could not tell a device that counts as a second factor from an enrollment whose code was never confirmed: `POST /api/mfa/enrollment` saves the device as enrolled (`associated=false`) before any code is checked, the listing returned every device, and the response had no `associated` field. The listing also returned an empty list whenever the account had no default device.

- **The listing returns associated devices unless asked otherwise.** `GET /api/mfa/enrollment` takes `associated=true|false|all`, default `true`. `true` lists only associated devices, `false` only enrolled ones not associated yet, `all` both; any other value is `400`. Every device in the response carries `associated`. The login MFA page calls the endpoint without the parameter, so it offers only devices it can actually challenge.
- **No default device is not an empty list.** The devices are listed whether or not one of them is the default; `default` is `false` on each of them in that case.
- **A verified email is an associated device, and the default one when the account has none.** The email verification link already associates the email device as the default (ADR 0013). An account created with its email already verified, today only the admin account of `POST /actuator/tenant-setup`, never gets a link to click, so it used to keep an enrolled email device and no default device: no login challenge was sent to it. Now `SendVerifyEMailChallenge` associates such an account's email device at once, sets it as the default device when the account has none, and sends no verification mail. With association setting a default when there is none (#339) and revocation refusing the default device (ADR 0009), an account with an associated device always has a default one, so the listing, the login challenge and revocation all read the same stored default.

**Considered options:**
- Add `associated` to the response and keep returning every device — rejected: every caller, including the login MFA page, would have to filter, and a forgotten filter offers a device that cannot pass a challenge.
- Resolve a missing default at read time, falling back to the email device whose channel is the user name — rejected: the only account without a default had an email device that was not associated, which the verifier refuses and the listing hides, and the revocation check reads the stored default in the adapter, so the fallback would not have been seen there.
- An enum-valued parameter (`ASSOCIATED`, `NOT_ASSOCIATED`, `ALL`) — rejected: `true|false|all` reads as the field it filters on.

**Consequences:**
- Breaking for API callers that relied on seeing enrolled devices: they pass `associated=all` (or `false`). VAuthenticator is in alpha and no migration is provided.
