# MFA


## Abstract

In order to increase account security VAuthenticator allows registering MFA via email and SMS.

## How to

Sequence diagrams of each use case, from the endpoint to the stores, are in [mfa-sequence-diagrams.md](mfa-sequence-diagrams.md).

At login, MFA is a step of the login workflow: a client application with the ```mfa:always``` scope gets an authorization code only once the session has passed the MFA challenge, and a challenge passed once counts for every such application for the rest of the session. See [login-workflow.md](login-workflow.md).

**A client application with ```mfa:always``` must also have ```mfa:enrollment```.** The login MFA page lists the account's devices with ```GET /api/mfa/enrollment```, authorized by the scopes of the client application in the browser session. Without ```mfa:enrollment``` that call gets ```403```, the page has no device to submit, and every code is rejected as wrong or expired (the server logs ```MfaDeviceNotFoundException```).

During the account registration a step to validate the main mail, that is the user_name too, the main mail became a valid MFA channel.
It is possible to register multiple email via api.

*Authentication:* Enrollment, Association, Set Default Device and Revoke Device require a bearer JWT. A call without one, including from a logged-in browser session, gets ```401 Unauthorized``` with ```WWW-Authenticate: Bearer```. A JWT without the required scope gets ```403 Forbidden```. List Enrolled Devices and Challenge also accept the browser session used by the MFA pages: without a bearer JWT, the scope is checked on the session's client application (see ADR ```docs/adr/0007-jwt-only-api-authentication-per-controller.md```).

### Enrollment


*URI:* ```Post /api/mfa/enrollment```

*Scope:* ```mfa:enrollment```

*Request:*

```json
{
  "mfaChannel": "the destination the code is sent to, e.g. an email address or a phone number",
  "mfaMethod": "EMAIL_MFA_METHOD, SMS_MFA_METHOD"
}
```
*Response Body:*

```json
{
  "ticket": "xxxx"
}
```
*Response Status:* ```201 Created```

### Association

*URI:* ```Post /api/mfa/associate```
*Scope:* ```mfa:enrollment```
*Request Body:*

```json
{
  "ticket": "your mfa ticket",
  "code": "your mfa code"
}
```

*Response Status:* ```204 No Content```

### List Enrolled Devices

*URI:* ```Get /api/mfa/enrollment```

*Scope:* ```mfa:enrollment``` (from the JWT, or from the session's client application for browser callers)

The login MFA page calls this endpoint, so a client application that uses MFA login (```mfa:always```) must also have ```mfa:enrollment```.

Returns the MFA devices enrolled for the authenticated account. The `userName` and `mfaChannel` values are returned masked.

*Response Body:*

```json
[
  {
    "userName": "axxxx@email.com",
    "mfaMethod": "EMAIL_MFA_METHOD",
    "mfaChannel": "axxxx@email.com",
    "mfaDeviceId": "xxxx",
    "default": true
  }
]
```
*Response Status:* ```200 OK```

### Set Default Device

*URI:* ```Put /api/mfa/device```

*Scope:* ```mfa:enrollment```

*Request Body:*

```json
{
  "mfaDeviceId": "the id of the enrolled device to set as default"
}
```

The device must be an associated device of the authenticated account, otherwise ```404 Not Found```.

*Response Status:* ```204 No Content```

### Revoke Device

Revokes one of the authenticated account's MFA devices: the device and its key are deleted, and enrolling the same channel again creates a new device. See ```docs/adr/0009-mfa-device-revocation-keeps-a-usable-second-factor.md```.

*URI:* ```Delete /api/mfa/enrollment/{mfaDeviceId}```

*Scope:* ```mfa:enrollment```

*Response Status:*

- ```204 No Content```: the device is revoked
- ```404 Not Found```: the account has no device with this id (a device of another account is reported the same way)
- ```409 Conflict```: the revocation is refused, with the reason in the body:
  - ```LAST_ASSOCIATED_DEVICE```: it is the account's last associated device. Enrolled devices that were never associated can always be revoked.
  - ```DEFAULT_DEVICE```: it is the account's default device; set another device as default first

  When both apply (the account's only associated device is also its default), the reason is ```LAST_ASSOCIATED_DEVICE```.

```json
{
  "reason": "DEFAULT_DEVICE"
}
```

*Example:*

An account has an email device (the default) and a phone device, and wants to drop the email one. The calls use a bearer JWT with scope ```mfa:enrollment```; the host is the local one from ```local/http-client.env.json```.

1. List the devices to find the ids:

   ```bash
   curl -H "Authorization: Bearer $TOKEN" \
     http://local.api.vauthenticator.com:9090/api/mfa/enrollment
   ```

   ```json
   [
     { "userName": "axxxx@email.com", "mfaMethod": "EMAIL_MFA_METHOD", "mfaChannel": "axxxx@email.com", "mfaDeviceId": "EMAIL_DEVICE_ID", "default": true },
     { "userName": "axxxx@email.com", "mfaMethod": "SMS_MFA_METHOD", "mfaChannel": "+39 233xxxxx233", "mfaDeviceId": "PHONE_DEVICE_ID", "default": false }
   ]
   ```

2. Revoking the email device now is refused, because it is the default:

   ```bash
   curl -i -X DELETE -H "Authorization: Bearer $TOKEN" \
     http://local.api.vauthenticator.com:9090/api/mfa/enrollment/EMAIL_DEVICE_ID
   ```

   ```
   HTTP/1.1 409 Conflict

   {"reason":"DEFAULT_DEVICE"}
   ```

3. Make the phone the default device:

   ```bash
   curl -X PUT -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
     -d '{"mfaDeviceId": "PHONE_DEVICE_ID"}' \
     http://local.api.vauthenticator.com:9090/api/mfa/device
   ```

4. Revoke the email device:

   ```bash
   curl -i -X DELETE -H "Authorization: Bearer $TOKEN" \
     http://local.api.vauthenticator.com:9090/api/mfa/enrollment/EMAIL_DEVICE_ID
   ```

   ```
   HTTP/1.1 204 No Content
   ```

   Revoking the phone device afterwards would return ```409``` with ```LAST_ASSOCIATED_DEVICE```, since it is now the account's only associated device.

On the ```database``` profile the check and the delete hold the account's devices locked. On the ```dynamo``` profile they do not: two concurrent revocations on the same account can both pass the check and leave it without an associated device. Such an account cannot complete an MFA login until its devices are repaired.

### Challenge

Sends an MFA challenge code to the account's default enrolled device, or to a specific device when `mfa-device-id` is provided. Used at login time to (re)send the code.

*URI:* ```Put /api/mfa/challenge```

*Scope:* ```mfa:always```

*Query Parameters:*

- `mfa-device-id` (optional): the id of a specific enrolled device to send the challenge to. When omitted, the challenge is sent to the account's default device.

*Response Status:* ```200 OK```

### SMS Challenge Text

The SMS a challenge sends is rendered from the MFA SMS template, the same way an email challenge is rendered from the MFA email template. The template is a Jinjava text with the account fields (```firstName```, ```lastName```, ```email```, ...) plus ```phone``` and ```mfaCode``` in context, for example:

```
Your VAuthenticator verification code is {{ mfaCode }}
```

The template is stored in the document store at ```sms/templates/mfa-challenge.txt```. **An SMS challenge fails until that template exists**, as an email challenge fails without its email template. The default template is in ```communication/default/sms/mfa-challenge.txt```: ```local/build-frontend.sh``` copies it into the local file-system store, while an S3 deployment must upload it to ```sms/templates/mfa-challenge.txt``` in the document bucket.

The template is read and replaced through the SMS template API:

*URI:* ```Get /api/sms-template/{smsType}``` (```smsType```: ```MFA```)

*Scope:* ```admin:sms-template-reader```

*Response Body:*

```json
{ "smsType": "MFA", "body": "Your VAuthenticator verification code is {{ mfaCode }}" }
```

*URI:* ```Put /api/sms-template```

*Scope:* ```admin:sms-template-writer```

*Request:* the same body as the ```GET``` response.

*Response Status:* ```204 No Content```

Both endpoints accept a bearer JWT only, as the email template endpoints do: a browser session gets ```401```, a JWT without the scope gets ```403```, and ```admin:full-access``` covers both. Like ```admin:email-template-writer```, ```admin:sms-template-writer``` lets its holder write Jinjava templates, so grant it only to trusted admin clients.

### Verification

A login challenge is verified against the device named by ```mfa-device-id```, or against the account's default device. The verification fails when that device does not exist, belongs to another account, or is not associated, and when the account has no default device. An association ticket fails when its device no longer exists.

A verified login challenge marks MFA as passed in the session and resumes the authorization request that asked for it (see [login-workflow.md](login-workflow.md)).
