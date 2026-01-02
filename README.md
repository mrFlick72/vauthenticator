# VAuthenticator

This project is actually a journey. This is a big evolution of the OAuth2 authorization server 
developed during my master thesis to an OpenID Connect authentication server.
In this new version I expand the initial project in order to use JWT token embracing OpenID Connect protocol,
all written in Kotlin based on Spring Boot 3.x and more over to the latest spring based oauth2.1/openid connect framework:
[spring-authorization-server](https://github.com/spring-projects-experimental/spring-authorization-server). 

## The Architecture

![](https://github.com/mrFlick72/vauthenticator/blob/main/images/vauthenticator-architecture.png)

## Features
Right now it is based, as said before to the latest version on spring oauth2/open id connect framework [spring-authorization-server](https://github.com/spring-projects-experimental/spring-authorization-server).

**API:**

- Client Applications management  
- roles management
- account management
- sign up: admin:signup scope is required
- welcome mail: admin:welcome scope is required
- email verification: admin:mail-verify scope is required
- reset password: admin:reset-password scope is required
- access_token/id_token customization via lambda, see [here](auth-server/docs/lambda.md) for more details
- MFA
  - mail
  - sms
  - see [here](auth-server/docs/mfa.md) for more details
- Post login flow 
  - force to reset password
- back/front channel logout
- management api: custom actuator endpoint for more details [look here](auth-server/docs/management.md)

**Storage:**

- DynamoDB 
- Redis:
  - authorization code
  - distributed session store
  - distributed cache
- RSA key pair are created from KMS Customer Master Key stored on Dynamo, private key encrypted via KMS of course stored on Dynamo.


### auth server local environment

For more details please follow to this link [readme.md](auth-server/local/readme.md)


### management ui local environment

In order to build the application, you can use the build.sh script under managament-ui folder.
The ```docker-compose.yml``` file under the local folder cna be used to sin up a nginx exposed to the port 8085

To access to the application you can use the following link: **http://local.management.vauthenticator.com:8085/secure/admin/index**, please take care to have the local ip mapped to the ```local.management.vauthenticator.com``` in the host file.


### profiling

The application configuration is very versatile and you can decide what persistence and key management provider to use AWS or not AWS native. 
For more details please refer to the detailed page [here](auth-server/docs/profiles.md)