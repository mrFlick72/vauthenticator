# MFA


## Abstract

In order to increase account security VAuthenticator allow to register MFA via email and soon SMS

## How to

During the account registration a step to validate the main mail, that is the user_name too, the main mail became a valid MFA channel.
It is possible to register multiple email via api.

### Enrollment


*URI:* ```Post /api/mfa/enrollment```

*Scope:* ```mfa:enrollment```

*Request:*

```json
{
  "mfaChannel": "your mfa channel: mail sms and so on ",
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