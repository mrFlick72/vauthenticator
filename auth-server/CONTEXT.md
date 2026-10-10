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
