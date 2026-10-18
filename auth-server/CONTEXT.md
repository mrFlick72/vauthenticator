# VAuthenticator Authorization Server

The OAuth2/OIDC authorization server and the account, password, MFA and role concerns that sit behind it.

## Language

### Events

**System Client**:
The client identity that server-initiated actions (such as scheduled jobs) are attributed to when no real client app is involved.
_Avoid_: anonymous client, null client

### Password lifecycle

**Password Lifecycle Rule**:
An admin-assigned timer on an account that applies a **Lifecycle Action** when its **Evaluation Interval** elapses. An account has at most one rule per **Lifecycle Action**; a rule belongs to its account and cannot exist without it.
_Avoid_: password expiry, password policy

**Evaluation Interval**:
The period between two consecutive firings of a **Password Lifecycle Rule**, counted from the rule's registration (or re-registration) for the first firing and from its **Last Evaluation** afterwards. It has a fixed length, expressed in seconds, and is not tied to the password's age.
_Avoid_: password age, expiry

**Last Evaluation**:
The moment a **Password Lifecycle Rule** last fired and applied its **Lifecycle Action**; it anchors the next **Evaluation Interval**.

**Account Pattern**:
A case-insensitive username pattern that selects accounts, where `*` matches any run of characters and everything else is literal; `*` alone selects every account. Registering a rule for an **Account Pattern** gives each account matching at that moment its own **Password Lifecycle Rule**, replacing any rule the account already has for that action; accounts created later are not covered.
_Avoid_: wildcard rule, group rule

**Lifecycle Action**:
What a **Password Lifecycle Rule** does to the account when it fires: force a password reset (recurring — fires every interval), or lock the account (one-shot — fires once, then the rule is spent).

### MFA

**MFA Device**:
One **MFA Method** on one **MFA Channel**, registered to an account under its own id. Within an account a channel identifies at most one device; a removed device's id is never reused, so enrolling the same channel again yields a new device.
_Avoid_: MFA method (for the device), enrollment (as a noun)

**MFA Method**:
The kind of second factor a device uses: email or SMS.

**MFA Channel**:
The destination codes are sent to: an email address or a phone number.

**Enrolled**:
A device that is registered but whose code has not yet been confirmed. It does not count as a second factor.

**Associated**:
A device whose code has been confirmed by the account holder. Only associated devices count as a second factor at login.

**Default Device**:
The associated device a challenge goes to when the caller does not name one.

**Revoke**:
The account holder takes a device off their account; it can no longer receive or verify challenges. An account's last associated device cannot be revoked, nor can its **Default Device** until another device is made default; enrolled devices can always be revoked. Revoking a device is distinct from revoking a ticket or a token.
_Avoid_: remove, delete
