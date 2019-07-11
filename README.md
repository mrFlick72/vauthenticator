# VAuthenticator

This project is actually a journey. This is a big evolution of the OAuth2 authorization server 
developed during the my master thesis to an OpenID Connect authentication server.
In this new version I expand the initial project in order to use JWT token embracing OpenID Connect protocol,
all written in Kotlin based on Spring Boot 2.x. 

## The Architecture

![](https://github.com/mrFlick72/vauthenticator/blob/master/images/vauhenticator-architecture.png)

## Sample Application

In order to try VAuthenticator, in this repo you can find a no-configserver-configuration folder with all the basic 
configuration in order to start up the application without a config server and the needed keystore for the RSA key pair.
The only thing that you should do is provide these external configurations:

```properties
project.basedir=your project path
spring.cloud.bootstrap.location=${project.basedir}/no-configserver-configuration/bootstrap.yml;
```  

Since that the user data source in VAuthenticator is provided by a microservice and not a classical datasource like: ldap, 
database and so on the repo provides a very simple account service suitable for testing under testable-account-service maven project.

Using these two applications: VAuthenticator started whit the properties like above and the testable-account-service project, 
you should be able to use VAuthenticator for your sample application. The preconfigured user in testable-account-service is a 
user with `user` as username and `secret` as password 

It is provided an all-in-one testable solution with a docker-compose under testable-docker-infrastructure folder the only thing to do 
is configure CONFIGURATION_FOLDER environment variable in a .env file with the path of the no-configserver-configuration folder in your 
local environment.

## Features

The application is a Spring Cloud application build on the top of Spring Cloud Security in order to leverage the typical 
OAuth2 Authorization server. On the top of OAuth2 feature the VAuthenticator provide JWK endpoint for RSA key exchange 
with keystore storage on file system or AWS S3, token enhancement of OpenID Connect id_token for client application 
that has `openid` as scope and a well known OpenID Connect discovery endpoint.  

As Client Application Store Postgress Database is used while for token store, authorization code store, distributed session store and 
distributed cache Redis is the my choice. 

At the moment the application try to leverage the configuration from a configuration server discovered by Netflix Eureka.
The User datasource as said above is supposed to provided by an `account-service`, the supported communication between 
VAuthenticator and the account-service is via a secure RabbitMq AMQP channel, at the moment the object required is a 
Spring Security UserDetails implementation, but this details is under develop in order to remove this coupling between 
account-service and VAuthenticator to a Spring UserDetails implementation.

## Next Features

* ~~User details exchanged between a generic account-service and VAuthenticator independent from Spring Security~~
* Token enhancer 
* Client application management
* Password recovery system
* OpenID Connect Global Login
* OpenID Connect Global Logout