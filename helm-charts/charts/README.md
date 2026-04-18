# VAuthenticator Helm Chart

It is the official Helm chart for the VAuthenticator authorization server.

# Global Components

We discuss how to configure global and generally available properties. AWS, Redis, KEDA, pod probe settings, and the
`po` block are root-level settings, while deployment-specific labels, selectors, and service settings are applied
through the `application` section.

## Redis

It is possible to enable the installation via helm of a namespace scoped redis.
In order to enable it have a look to the yaml snippet below and refers to the
official [bitnami details](https://github.com/bitnami/charts/tree/main/bitnami/redis/)

#### yaml section

```yaml
in-namespace:
  redis:
    enabled: true

```

#### Properties description

| Name                       | Description                         | Value |
|----------------------------|-------------------------------------|-------|
| in-namespace.redis.enabled | if install a redis in k8s namespace | true  |

## AWS

VAuthenticator is designed with AWS and kubernetes in mind. In order to configure AWS credentials have a look below:

#### yaml section

```yaml
aws:
  region: xxxxxxxxx
  iamUser:
    accessKey: xxxxxxxxx
    secretKey: xxxxxxxxx
    enabled: false
  eks:
    serviceAccount:
      enabled: false
      iamRole:
        arn: arn:aws:iam::ACCOUNT_ID:role/IAM_ROLE_NAME

```

#### Properties description

| Name                               | Description                                                                                                                                                                | Value                                      |
|------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|
| aws.region                         | AWS Region like eu-central-1                                                                                                                                               | xxxxxxxxx                                  |
| aws.iamUser.accessKey              | AWS IAM User Access Key Id                                                                                                                                                 | xxxxxxxxx                                  |
| aws.iamUser.secretKey              | AWS IAM User Access Key secret                                                                                                                                             | xxxxxxxxx                                  |
| aws.iamUser.enabled                | Identify that we want use a dedicated AWS IAM user. This strategy is required if VAuthenticator is deployed on premise                                                     | false                                      |
| aws.eks.serviceAccount.enabled     | Identify that we want use a dedicated Service Account linked to an IAM identity Provider. <br/>This strategy is the best practice if VAuthenticator is deployed on AWS EKS | false                                      |
| aws.eks.serviceAccount.iamRole.arn | It is the arn of the role used from STS to gain permissions                                                                                                                | arn:aws:iam::ACCOUNT_ID:role/IAM_ROLE_NAME |

## KEDA

In order to apply autoscaling in VAuthenticator authorization server is possible configure [keda](https://keda.sh/) with
prometheus metrics.

#### yaml section

```yaml
keda:
  enabled: false
  spec:
    minReplicaCount: 1
    maxReplicaCount: 1
    pollingInterval: 1
    cooldownPeriod: 300
  prometheus:
    serverAddress: ""
    metricName: ""
    threshold: ""
    query: ""
```

#### Properties description

| Name                                      | Description                                                                                                           | Value |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|-------|
| keda.enabled                              | define if the keda autoscaling is enabled                                                                             | false |
| keda.spec.minReplicaCount                 | minReplicaCount autoscaling parameter                                                                                 | 1     |
| keda.spec.maxReplicaCount                 | maxReplicaCount autoscaling parameter                                                                                 | 1     |
| keda.spec.pollingInterval                 | polling Interval of metric evaluation in order to decide if apply autoscaling or not                                  | 1     |
| keda.spec.cooldownPeriod                  | specifies how long any alarm-triggered scaling action will be disallowed after a previous scaling action is complete. | 300   |
| keda.prometheus.serverAddress             | prometheus address                                                                                                    | ""    |
| keda.prometheus.metricName                | prometheus metric name                                                                                                | ""    |
| keda.prometheus.threshold                 | prometheus metric threshold to apply for autoscaling                                                                  | ""    |
| keda.prometheus.query                     | prometheus query to evaluate autoscaling                                                                              | ""    |

## POD

#### yaml section

```yaml

pod:
  probes:
    liveness:
      initialDelaySeconds: 10
      periodSeconds: 30
    rediness:
      initialDelaySeconds: 10
      periodSeconds: 30
```

#### Properties description

| Name                                    | Description                                                                                    | Value |
|-----------------------------------------|------------------------------------------------------------------------------------------------|-------|
| pod.probes.liveness.initialDelaySeconds | define for liveness probe the initial delay in seconds to wait to start to evaluate the probes | 10    |
| pod.probes.liveness.periodSeconds       | define for liveness probe period in seconds to wait for the next evaluation                    | 30    |
| pod.probes.rediness.initialDelaySeconds | define for rediness probe the initial delay in seconds to wait to start to evaluate the probes | 10    |
| pod.probes.rediness.periodSeconds       | define for rediness probe period in seconds to wait for the next evaluation                    | 30    |

## Pod Annotations

#### yaml section

```yaml
pod:
  podAnnotations: { }
```

#### Properties description

| Name               | Description                             | Value |
|--------------------|-----------------------------------------|-------|
| pod.podAnnotations | define pod annotations if required      | { }   |

## Ingress (application scoped)

#### yaml section

```yaml
ingress:
  host: "*"
  annotations: { }
  tls: { }
  enabled: true
  class: nginx
```

#### Properties description

| Name                | Description                                                                     | Value |
|---------------------|---------------------------------------------------------------------------------|-------|
| ingress.host        | define on what host the ingress resource should be configured to answer         | *     |
| ingress.annotations | define annotations for the ingress resource                                     | { }   |
| ingress.tls         | define tls configuration for the ingress resource                               | { }   |
| ingress.enabled     | define if the ingress should be included in the resources                       | true  |
| ingress.class       | define what kind of ingress class should be configured for teh ingress resource | nginx |

## Gateway API HTTPRoute (application scoped)

The chart can expose VAuthenticator through a Gateway API `HTTPRoute`. When `gateway.enabled` is `true`, the chart
expects at least one `gateway.parentRefs` entry that points to an existing Gateway listener. If `gateway.rules` is
left empty, the chart renders a default `PathPrefix /` rule that forwards to the VAuthenticator service on
`application.server.port`.

For TLS with cert-manager, configure TLS on the referenced Gateway listener itself, for example with an HTTPS listener,
cert-manager annotations on the Gateway, and `tls.certificateRefs`. The `HTTPRoute` then attaches to that listener and
matches the same hostname.

#### yaml section

```yaml
gateway:
  enabled: false
  annotations: { }
  labels: { }
  parentRefs:
    - name: traefik-gateway
      namespace: traefik
      sectionName: websecure # Optional: target a specific Gateway listener
  hostnames:
    - auth.example.com # Should match the HTTPS listener hostname/certificate DNS name
  rules: [ ]
```

#### Properties description

| Name                | Description                                                                                                            | Value                           |
|---------------------|------------------------------------------------------------------------------------------------------------------------|---------------------------------|
| gateway.enabled     | define if the Gateway API `HTTPRoute` should be included in the resources                                             | false                           |
| gateway.annotations | define annotations for the `HTTPRoute` resource                                                                       | { }                             |
| gateway.labels      | define additional labels for the `HTTPRoute` resource                                                                 | { }                             |
| gateway.parentRefs  | define the target Gateway references. At least one entry is required when `gateway.enabled` is `true`                | [ ]                             |
| gateway.hostnames   | define the optional list of hostnames matched by the `HTTPRoute`                                                      | [ ]                             |
| gateway.rules       | define the `HTTPRoute.spec.rules` list. If empty, the chart renders a default `/` `PathPrefix` rule to the service   | [ ]                             |

## Pod Resources (application scoped)

#### yaml section

```yaml

resources:
  requests:
    cpu: "256m"
    memory: "256Mi"
  limits:
    cpu: "512m"
    memory: "512Mi"

```

#### Properties description

| Name                      | Description                               | Value   |
|---------------------------|-------------------------------------------|---------|
| resources.requests.cpu    | usual cpu kubernetes request parameter    | "256m"  |
| resources.requests.memory | usual memory kubernetes request parameter | "256Mi" |
| resources.limits.cpu      | usual cpu kubernetes limits parameter     | "512m"  |
| resources.limits.memory   | usual memory kubernetes limits parameter  | "512Mi" |

## Image (application scoped)

#### yaml section

```yaml

image:
  repository: mrflick72/vauthenticator-k8s
  pullPolicy: Always
  tag: "latest"

```

#### Properties description

| Name             | Description                               | Value      |
|------------------|-------------------------------------------|------------|
| image.repository | usual cpu kubernetes request parameter    | it depends |
| image.pullPolicy | usual memory kubernetes request parameter | Always     |
| image.tag        | usual cpu kubernetes limits parameter     | "latest"   |

## Replicas count, Lables and selectors (application scoped)

#### yaml section

```yaml
service:
  type: ClusterIP
replicaCount: 1

lables: { }

selectorLabels:
  app: vauthenticator
```

#### Properties description

| Name           | Description                                                                                   | Value      |
|----------------|-----------------------------------------------------------------------------------------------|------------|
| replicaCount   | define how many pod replicas you want                                                         | 1          |
| service.type   | **do not touch this this** it is used internally                                              | ClusterIP  |
| selectorLabels | **do not touch this this** it is used internally as common selector for pod service and so on | it depends |
| lables         | define pod lables if requred                                                                  | { }        |

# VAuthenticator Authorization Server

## Authorization Server

Authorization server backend application configuration

#### yaml section

```yaml
application:
  sessionTimeout: 24h
  profiles: dynamo,kms
  datasource:
    url: jdbc:postgresql://db.local.vauthenticator.com:5432/
    username: postgres
    password: postgres
  password:
    history:
      evaluationLimit: 1
      maxHistoryAllowedSize: 3
    generatorCriteria:
      upperCaseCharactersSize: 2
      lowerCaseCharactersSize: 2
      specialCharactersSize: 2
      numberCharactersSize: 2
    policy:
      minSize: 1
      minSpecialSymbol: 1
      enablePasswordReusePrevention: true

  masterKey: ACCOUNT_KMS_KEY
  masterKeyContent: xxxxxxxxx

  redis:
    database: 0
    host: vauthenticator-redis-master.auth.svc.cluster.local

  server:
    port: 8080

  baseUrl: http://application-example-host.com


  emailProvider:
    host: localhost
    port: 587
    username: ""
    password: ""
    properties: { }

  email:
    from: ""
    welcomeEMailSubject: ""
    verificationEMailSubject: ""
    resetPasswordEMailSubject: ""
    mfaEMailSubject: ""

  dynamoDb:
    account:
      tableName: your_VAuthenticator_Account_table_name
    role:
      tableName: your_VAuthenticator_Role_table_name
    clientApplication:
      tableName: your_VAuthenticator_ClientApplication_table_name
    mfaAccountMethods:
      tableName: your_VAuthenticator_mfaAccountMethods_table_name
    keys:
      mfa:
        tableName: your_VAuthenticator_Mfa-Keys_table_name
      signature:
        tableName: your_VAuthenticator_Signature_Keys_table_name
      tableName: your_VAuthenticator_Keys_table_name
    ticket:
      tableName: your_VAuthenticator_tICKET_table_name

    documentRepository:
      engine: s3
      bucketName: test
      fsBasePath: dist
      documentType:
        mail:
          cacheName:
          cacheTtl: 1m
        staticAsset:
          cacheName:
          cacheTtl: 1m

  mfa:
    otp:
      otpLength: 6
      otpTimeToLiveInSeconds: 30

  assetServer:
    onS3:
      enabled: false
    baseUrl: http://localhost:8080

  events:
    enableLoggerConsumer: false

```

`spring.datasource` is rendered in the generated `application.yml` only when `application.profiles` includes
`database`. In that case `application.datasource.url` is required.

#### Properties description

| Name                                                                  | Description                                                                                                                                | Value                                                   |
|-----------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| application.sessionTimeout                                            | Server Session Timeout expressed as java duration                                                                                          | 24h                                                     |
| application.profiles                                                  | defines the activated spring profiles, useful to switch from dynamo to database or local java to kms implementation                       | dynamo,kms                                              |
| application.datasource.url                                            | Spring datasource JDBC URL. Required when `application.profiles` includes `database`                                                      | ""                                                      |
| application.datasource.username                                       | Spring datasource username                                                                                                                 | ""                                                      |
| application.datasource.password                                       | Spring datasource password                                                                                                                 | ""                                                      |
| application.masterKey                                                 | KMS Master key as starting key to generate data key data key pair to sign tokens, MFA and data encryption                                  | your kms key id                                         |
| application.redis.database                                            | Redis database used by vauthenticator authorization server                                                                                 | 0                                                       |
| application.redis.host                                                | Redis database host used by vauthenticator authorization server                                                                            | vauthenticator-redis-master.auth.svc.cluster.local      |
| application.server.port                                               | standard port in which the main tomcat is exposed <br/>**do not touch it!** it is useless lets to use ingress to access to the main tomcat | 8080                                                    |
| application.baseUrl                                                   | public url to reach the authorization server                                                                                               |                                                         |
| application.emailProvider.host                                        | mail server host                                                                                                                           | localhost                                               |
| application.emailProvider.port                                        | mail server used port                                                                                                                      | 587                                                     |
| application.emailProvider.username                                    | mail server account username                                                                                                               | ""                                                      |
| application.emailProvider.password                                    | mail server account password                                                                                                               | ""                                                      |
| application.emailProvider.properties                                  | mail server additional properties                                                                                                          | { }                                                     |
| application.email.from                                                | mail form default field                                                                                                                    | ""                                                      |
| application.email.welcomeEMailSubject                                 | subject for welcome mail                                                                                                                   | ""                                                      |
| application.email.verificationEMailSubject                            | subject for account verification mail                                                                                                      | ""                                                      |
| application.email.resetPasswordEMailSubject                           | subject for account password reset                                                                                                         | ""                                                      |
| application.email.mfaEMailSubject                                     | subject for account mfa verification0                                                                                                      | ""                                                      |
| application.dynamoDb.account.tableName                                | Account Table Name                                                                                                                         | your_VAuthenticator_Account_table_name                  |
| application.dynamoDb.account.cache.ttl                                | Account Table Redis Cache TTL                                                                                                              | 1h                                                      |
| application.dynamoDb.account.cache.name                               | Account Table Redis Cache Region Name                                                                                                      | account_cache                                           |
| application.dynamoDb.role.tableName                                   | Roles Table Name                                                                                                                           | your_VAuthenticator_Role_table_name                     |
| application.dynamoDb.role.cache.ttl                                   | Role Table Redis Cache TTL                                                                                                                 | 1h                                                      |
| application.dynamoDb.role.cache.name                                  | Role Table Redis Cache Region Name                                                                                                         | role_cache                                              |
| application.dynamoDb.clientApplication.tableName                      | Client Applications Table Name                                                                                                             | your_VAuthenticator_ClientApplication_table_name        |
| application.dynamoDb.clientApplication.cache.ttl                      | Client Applications Table Redis Cache                                                                                                      | 1k                                                      |
| application.dynamoDb.clientApplication.cache.name                     | Client Applications Table Redis Region Name                                                                                                | client_application                                      |
| application.dynamoDb.mfaAccountMethods.tableName                      | MFA Account Methods Table Name                                                                                                             | your_VAuthenticator_mfaAccountMethods_table_name        |
| application.dynamoDb.defaultMfaAccountMethods.tableName               | MFA Default Account Methods Table Name                                                                                                     | your_VAuthenticator_defaultMfaAccountMethods_table_name |
| application.dynamoDb.keys.mfa.tableName                               | MFA keys Table Name                                                                                                                        | your_VAuthenticator_Mfa_Keys_table_name                 |
| application.dynamoDb.keys.signature.tableName                         | Token Signature Keys Table Name                                                                                                            | your_VAuthenticator_Signature_Keys_table_name           |
| application.dynamoDb.ticket.tableName                                 | Ticket Table Name                                                                                                                          | your_VAuthenticator_Ticket_table_name                   |
| application.dynamoDb.passwordHistory.tableName                        | Password History Table Name                                                                                                                | your_VAuthenticator_Password_History_table_name         |
| application.dynamoDb.passwordHistory.historyEvaluationLimit           | Password History evaluation limit                                                                                                          | 1                                                       |
| application.dynamoDb.passwordHistory.maxHistoryAllowedSize            | Password History max history entry allowed                                                                                                 | 3                                                       |
| application.mfa.otp.otpLength                                         | mfa otp code length                                                                                                                        | 6                                                       |
| application.mfa.otp.otpTimeToLiveInSeconds                            | mfa otp ttl                                                                                                                                | 30                                                      |
| application.assetServer.baseUrl                                       | asset server for login, mfa and other pages                                                                                                | http://localhost:8080/asset                             |
| application.assetServer.onS3.enabled                                  | enable asset serving form S3                                                                                                               | true/false                                              |
| application.documentRepository.engine                                 | what engine use to get documents S3 or FileSystem                                                                                          | s3/file-system                                          |
| application.documentRepository.bucketName                             | bucket used to store documents when S3 document engine is enabled                                                                          | your-bucket                                             |
| application.documentRepository.fsBasePath                             | file system base path to store documents when FileSystem document engine is enabled                                                        | your-base-path                                          |
| application.documentRepository.documentType.email.cacheName           | file local cache region name for mail documents                                                                                            | your-base-path                                          |
| application.documentRepository.documentType.email.cacheTtl            | file local cache region ttl for mail documents                                                                                             | your-base-path                                          |
| application.documentRepository.documentType.staticAsset.cacheName     | file local cache region name for static asset                                                                                              | your-base-path                                          |
| application.documentRepository.documentType.staticAsset.cacheTtl      | file local cache region ttl for static asset                                                                                               | your-base-path                                          |
| application.events.enableLoggerConsumer                               | enable the default logger consumer for the vauthenticator events                                                                           | your-base-path                                          |
| application.password.history.evaluationLimit                          | it defines how many stored historical password evaluate                                                                                    | 1                                                       |
| application.password.history.maxHistoryAllowedSize                    | it defines the max number of stored historical password for a specific user                                                                | 3                                                       |
| application.password.generatorCriteria.upperCaseCharactersSize        | it defines how many upper case characters to use to generate a ramdom password                                                             | 2                                                       |
| application.password.generatorCriteria.lowerCaseCharactersSize        | it defines how many lower case characters to use to generate a ramdom password                                                             | 2                                                       |
| application.password.generatorCriteria.specialCharactersSize          | it defines how many special characters to use to generate a ramdom password                                                                | 2                                                       |
| application.password.generatorCriteria.numberCharactersSize           | it defines how characters to use to generate a ramdom password                                                                             | 2                                                       |
| application.password.policy.minSize                                   | it defines what is the minimum accepted password length                                                                                    | 1                                                       |
| application.password.policy.minSpecialSymbol                          | it defines what is the minimum number of special characters to use for a password                                                          | 1                                                       |
| application.password.policy.enablePasswordReusePrevention             | enable the password reuse prevention feature                                                                                               | true                                                    |
