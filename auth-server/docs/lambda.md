# Lambda

## Abstract

In order to customize VAuthenticator you can use AWS Lambda, it is very similar as approach to AWS Cognito or Auth0. 
Right now is possible to customize access and id token claims via lambda. 

## How to

In order to enable access token and id token customization via lambda you need to configure the property `vauthenticator.lambda.aws.enabled` to `true`.
The Default Lambda Name is `vauthenticator-token-enhancer` but it can but customized via `vauthenticator.lambda.aws.function-name` property in the application.yml.

The Lambda has to be configured as you organization want like: AWS Console, cli, Terraform, AWS SAM and so on. 

The expected event body scheme is like below:

```json
{
  "user": {
    "sub": "",
    "email": "",
    "first_name": "",
    "last_name": "",
    "birth_date": "",
    "phone": "",
    "email_verified": "true|false",
    "roles": []
  },
  "general_context_claims": {
    "client_id": "your-client-app-id",
    "grant_flow": "all supported grant flow for access_token and id_token",
    "authorized_scope": []
  },
  "access_token_claims": {
    ....
  },
  "id_token_claims": {
    ....
  }
}
```
The expected lambda result has to follow the same scheme, keep in mind that `access_token_claims` is where the lambda can put the claims to add to the `access_token`, while id_token_claims is intended to do the sam but for the `id_token`.
