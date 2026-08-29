# MFA


## Abstract

In order to increase account security VAuthenticator allows registering MFA via email and SMS.

## How to

During the account registration a step to validate the main mail, that is the user_name too, the main mail became a valid MFA channel.
It is possible to register multiple email via api.

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

*Scope:* ```mfa:enrollment```

Returns the MFA devices enrolled for the authenticated account. The `userName` and `mfaChannel` values are returned masked.

*Response Body:*

```json
[
  {
    "userName": "a***@email.com",
    "mfaMethod": "EMAIL_MFA_METHOD",
    "mfaChannel": "a***@email.com",
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

*Response Status:* ```204 No Content```

### Challenge

Sends an MFA challenge code to the account's default enrolled device, or to a specific device when `mfa-device-id` is provided. Used at login time to (re)send the code.

*URI:* ```Put /api/mfa/challenge```

*Scope:* ```mfa:always```

*Query Parameters:*

- `mfa-device-id` (optional): the id of a specific enrolled device to send the challenge to. When omitted, the challenge is sent to the account's default device.

*Response Status:* ```200 OK```
