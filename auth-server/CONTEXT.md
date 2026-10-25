# VAuthenticator Authorization Server

The OAuth2/OIDC authorization server and the account, password, MFA and role concerns that sit behind it.

## Language

### Events

**System Client**:
The client identity that server-initiated actions (such as scheduled jobs) are attributed to when no real client app is involved.
_Avoid_: anonymous client, null client

### Login

**Login Workflow**:
The steps an account holder must pass, after proving their password, before a client app accepts the session: today the MFA challenge (for apps that require it) and the mandatory password change. The workflow is checked on every authorization request, not only right after the password: each **Passed Step** is skipped, every other step is evaluated again. Reaching a step's page does not pass it. Steps run in a fixed order (MFA before password change). A login with no client app asking runs only the steps that do not depend on an app; the others wait for the first authorization request.
_Avoid_: next hop

**Passed Step**:
A **Login Workflow** step the account holder actually completed in this session. It stays passed until the session ends (logout or expiry) or the password is proven again in the session (any new login starts the workflow fresh), and is not evaluated again on later authorization requests. A step that was skipped because it was not required is not passed; it is evaluated again next time.

**Login-Complete**:
A session is login-complete for a client app when every **Login Workflow** step that app requires has been passed. A session that proved the password but is not login-complete for the requesting app is sent back into the workflow instead of being logged into the app. It is judged per app: a session can be login-complete for one app and not for another.
_Avoid_: logged in (when the workflow is not complete), fully authenticated

**Second-Factor-Verified Session**:
A session in which the MFA challenge is a **Passed Step**. It counts for every client app that requires MFA for the rest of the session, so MFA is not presented again; a session that is login-complete only for an app without MFA is not second-factor-verified.
_Avoid_: MFA session, step-up

**Account Lock**:
A locked account cannot log in. A lock is lasting — it comes from signup (until the email is verified), an admin, or the lock **Lifecycle Action**, and stays until an explicit unlock (email verification or an admin) — unless it is a **Failed-Login Lock**.
_Avoid_: disabled (a separate account state)

**Failed-Login Lock**:
An **Account Lock** applied automatically when **Consecutive Failed Logins** reach the configured threshold, which ends on its own once the configured lock duration has passed. Only an unlocked account gets one: failed logins against an already-locked account never turn its lasting lock into one that expires. An explicit unlock ends it at once; an explicit lock of an account that is unlocked is lasting, while re-stating "locked" for an account under a **Failed-Login Lock** (e.g. an admin saving the account unchanged) leaves it temporary. A **Lifecycle Action** that locks an account under a **Failed-Login Lock** makes the lock lasting. While it lasts, every login is refused, even with the right password; those attempts are not **Consecutive Failed Logins** and do not extend the lock. It only stops new logins: sessions and tokens issued before it keep working, and a password reset neither lifts it nor is blocked by it.
_Avoid_: block, lockout

**Consecutive Failed Logins**:
The number of wrong passwords given for an account since its last successful password check. It resets to zero when the password is proven, when a **Failed-Login Lock** is applied or ends, or when no failure has occurred for the lock duration. Only the password step counts: a wrong MFA code is not a failed login. Attempts against usernames with no account count for nothing.
_Avoid_: failed attempts (unqualified)

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

### Communication

**SMS Type**:
The purpose an SMS is sent for; each type has exactly one **SMS Template**. Today the only type is the MFA challenge; more purposes may be added later.

**SMS Template**:
The admin-editable text an SMS of a given **SMS Type** is rendered from. It mirrors the email template of an email type: an SMS whose template has not been set up cannot be sent.
_Avoid_: SMS message (for the template)
