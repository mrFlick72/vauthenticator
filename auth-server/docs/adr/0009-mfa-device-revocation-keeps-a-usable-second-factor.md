# MFA device revocation keeps a usable second factor, and is a hard delete

For issue [#338](https://github.com/mrFlick72/vauthenticator/issues/338) the account holder can revoke one of their MFA Devices (`DELETE /api/mfa/enrollment/{mfaDeviceId}`, JWT only, scope `mfa:enrollment`, per ADR 0007). MFA is required by a client app's `mfa:always` scope, not chosen by the account, and there is no admin path to repair an account's devices. So revocation must never leave an account that cannot complete an `mfa:always` login.

- **The last associated device cannot be revoked** (`409`, reason `LAST_ASSOCIATED_DEVICE`). Enrolled devices that were never associated do not count and can always be revoked.
- **The Default Device cannot be revoked** (`409`, reason `DEFAULT_DEVICE`) until the account holder makes another device default with `PUT /api/mfa/device`. The server never picks a new default on its own. `PUT /api/mfa/device` accepts only an associated device of the caller's own account, so the default always points at an existing associated device.
- **A device of another account is reported as missing** (`404`), the same as an unknown id, so the endpoint does not reveal whether a device id exists.
- **Revocation is a hard delete** of the device record, then of its MFA key. An `MfaDeviceRevokedEvent` (device id and MFA Method, no channel) is the only trace left. Enrolling the same channel again creates a new device with a new id and key.

**Considered options:**
- Promote another associated device to default when the default is revoked: rejected. Nothing orders the devices, so the server would choose arbitrarily where the next login code goes.
- Protect the account's username email device instead of the last associated device: rejected. It also guarantees a device, but an account holder could never move to SMS only.
- Soft delete (mark revoked, keep the record and key): rejected. A kept key can still produce valid codes for a channel the account holder gave up, and nothing reads the old record; the event covers audit.

**Consequences:**
- A client that wants to revoke the default device first sets another default; the `409` reason tells it which rule applies.
- Revoking relies on MFA verification failing closed when a device cannot be resolved.
- The JDBC adapter checks and deletes in one transaction, locking the account's device rows. The DynamoDB adapter does not: two concurrent revokes can leave an account with no associated device. This is accepted for now and documented in `docs/mfa.md`; such an account cannot complete an MFA login until its devices are repaired.
- These rules are expected to be revisited when MFA stops being purely scope-driven, and together with step-up authentication for MFA management ([#395](https://github.com/mrFlick72/vauthenticator/issues/395)).
