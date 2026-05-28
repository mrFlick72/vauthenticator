# VAuthenticator Chart Values

This document describes the values for the `charts/vauthenticator` Helm chart.

The chart currently renders:

- `application`: the VAuthenticator authorization server
- `managementUi`: the management UI workload configured by the chart
- optional Redis dependency when `in-namespace.redis.enabled=true`

`config-manager` is not currently part of this chart.

## Development Commands

Run these commands from `helm-charts`:

```bash
helm dependency update charts/vauthenticator
helm lint charts/vauthenticator --set application.ingress.host=localhost --set managementUi.ingress.host=localhost
helm template vauthenticator charts/vauthenticator --set application.ingress.host=localhost --set managementUi.ingress.host=localhost
```

## Redis Dependency

```yaml
in-namespace:
  redis:
    enabled: true

redis:
  auth:
    enabled: false
  replica:
    replicaCount: 1
```

| Name | Description | Default |
| --- | --- | --- |
| `in-namespace.redis.enabled` | Install the Bitnami Redis dependency in the release namespace. | `true` |
| `redis.*` | Values passed to the Bitnami Redis subchart. | See `values.yaml` |

## AWS

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

| Name | Description | Default |
| --- | --- | --- |
| `aws.region` | AWS region. | `xxxxxxxxx` |
| `aws.iamUser.enabled` | Use explicit AWS access key and secret key environment variables. Useful outside EKS. | `false` |
| `aws.iamUser.accessKey` | AWS access key when `aws.iamUser.enabled=true`. | `xxxxxxxxx` |
| `aws.iamUser.secretKey` | AWS secret key when `aws.iamUser.enabled=true`. | `xxxxxxxxx` |
| `aws.eks.serviceAccount.enabled` | Use a Kubernetes service account intended for EKS IAM role integration. | `false` |
| `aws.eks.serviceAccount.iamRole.arn` | IAM role ARN used by the service account template. | `arn:aws:iam::ACCOUNT_ID:role/IAM_ROLE_NAME` |

## Common Workload Values

These groups exist under both `application` and `managementUi` unless noted.

### KEDA

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

| Name | Description | Default |
| --- | --- | --- |
| `*.keda.enabled` | Render a KEDA `ScaledObject`. | `false` |
| `*.keda.spec.minReplicaCount` | Minimum replica count. | `1` |
| `*.keda.spec.maxReplicaCount` | Maximum replica count. | `1` |
| `*.keda.spec.pollingInterval` | Metric polling interval. | `1` |
| `*.keda.spec.cooldownPeriod` | Cooldown period after scaling. | `300` |
| `*.keda.prometheus.*` | Prometheus trigger settings. | empty |

### Pod, Service, Ingress, Image, Resources

```yaml
pod:
  probes:
    liveness:
      initialDelaySeconds: 10
      periodSeconds: 30
    rediness:
      initialDelaySeconds: 10
      periodSeconds: 30

service:
  type: ClusterIP

ingress:
  host: "*"
  annotations: {}
  tls: {}
  enabled: true
  class: nginx

resources:
  requests:
    cpu: "256m"
    memory: "256Mi"
  limits:
    cpu: "512m"
    memory: "512Mi"

replicaCount: 1

image:
  repository: mrflick72/vauthenticator-k8s
  pullPolicy: Always
  tag: "0.8"

lables: {}
selectorLabels:
  app: vauthenticator
podAnnotations: {}
```

| Name | Description | Default |
| --- | --- | --- |
| `*.pod.probes.liveness.*` | Liveness probe timing. | `10`, `30` |
| `*.pod.probes.rediness.*` | Readiness probe timing. The value name is currently spelled `rediness` in the chart API. | `10`, `30` |
| `*.service.type` | Kubernetes service type. | `ClusterIP` |
| `*.ingress.host` | Ingress host. | `"*"` |
| `*.ingress.annotations` | Extra ingress annotations. | `{}` |
| `*.ingress.tls` | Ingress TLS block. | `{}` |
| `*.ingress.class` | Ingress class annotation value. | `nginx` |
| `*.ingress.enabled` | Present in defaults, but current templates always render Ingress resources. | `true` |
| `*.resources` | Container resource requests and limits. | See `values.yaml` |
| `*.replicaCount` | Deployment replica count. | `1` |
| `*.image.repository` | Container image repository. | workload-specific |
| `*.image.pullPolicy` | Container image pull policy. | `Always` |
| `*.image.tag` | Container image tag. | `0.8` |
| `*.lables` | Extra pod labels. The value name is currently spelled `lables` in the chart API. | `{}` |
| `*.selectorLabels` | Selector labels used by Deployment and Service. Change with care. | workload-specific |
| `*.podAnnotations` | Extra pod annotations. | `{}` |

## Authorization Server

```yaml
application:
  sessionTimeout: 24h
  profiles: dynamo,kms
  masterKey: ACCOUNT_KMS_KEY
  baseUrl: http://application-example-host.com
  backChannelBaseUrl: http://vauthenticator:8080
  server:
    port: 8080
  redis:
    database: 0
    host: vauthenticator-redis-master.auth.svc.cluster.local
```

| Name | Description | Default |
| --- | --- | --- |
| `application.sessionTimeout` | Server session timeout as a Spring duration. | `24h` |
| `application.profiles` | Active Spring profiles, for example `dynamo,kms` or `database`. | `dynamo,kms` |
| `application.masterKey` | Master key identifier used by key management configuration. | `ACCOUNT_KMS_KEY` |
| `application.baseUrl` | Public authorization server base URL. | `http://application-example-host.com` |
| `application.backChannelBaseUrl` | Internal service URL used for back-channel calls. | `http://vauthenticator:8080` |
| `application.server.port` | Application container port. | `8080` |
| `application.redis.database` | Redis database index. | `0` |
| `application.redis.host` | Redis host. This host is also used by the current management UI template. | `vauthenticator-redis-master.auth.svc.cluster.local` |

### Application AWS Endpoint Overrides

```yaml
application:
  aws:
    s3:
      endpointOverride:
    kms:
      endpointOverride:
    dynamodb:
      endpointOverride:
```

Use these values for LocalStack or non-default AWS endpoints.

### Password Policy

```yaml
application:
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
```

### Email

```yaml
application:
  emailProvider:
    enabled: false
    host: localhost
    port: 587
    username: ""
    password: ""
    properties: {}
  email:
    from: ""
    welcomeEMailSubject: ""
    verificationEMailSubject: ""
    resetPasswordEMailSubject: ""
    mfaEMailSubject: ""
```

`emailProvider` controls Spring mail configuration. The `email` block provides default sender and subject values used by communication flows when email is enabled.

### DynamoDB Tables

```yaml
application:
  dynamoDb:
    account:
      tableName: your_VAuthenticator_Account_table_name
      cache:
        ttl: 1h
        name: account_cache
      role:
        tableName: your_VAuthenticator_Account_Role_table_name
    role:
      tableName: your_VAuthenticator_Role_table_name
      cache:
        ttl: 1h
        name: role_cache
      protectedFromDelete: ROLE_USER,VAUTHENTICATOR_ADMIN
    clientApplication:
      tableName: your_VAuthenticator_ClientApplication_table_name
      cache:
        ttl: 1h
        name: client_application
    mfaAccountMethods:
      tableName: your_VAuthenticator_mfaAccountMethods_table_name
    defaultMfaAccountMethods:
      tableName: your_VAuthenticator_defaultMfaAccountMethods_table_name
    keys:
      mfa:
        tableName: your_VAuthenticator_Mfa_Keys_table_name
      signature:
        tableName: your_VAuthenticator_Signature_Keys_table_name
    ticket:
      tableName: your_VAuthenticator_Ticket_table_name
    passwordHistory:
      tableName: your_VAuthenticator_Password_History_table_name
      historyEvaluationLimit: 1
      maxHistoryAllowedSize: 3
```

These values configure the DynamoDB-backed repositories used by the `dynamo` profile.

### Documents, MFA, Assets, Events

```yaml
application:
  documentRepository:
    engine: s3
    bucketName: test
    fsBasePath: dist
    documentType:
      email:
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
      bundleVersion: ""
    baseUrl: http://localhost:8080
  events:
    enableLoggerConsumer: false
```

## Management UI Workload

The chart currently always renders the management UI manifests; there is no `managementUi.enabled` value in `values.yaml`.

```yaml
managementUi:
  redis:
    database: 1
    host: vauthenticator-redis-master.auth.svc.cluster.local
  server:
    port: 8080
  sso:
    clientApp:
      clientId: vauthenticator-management-ui
      clientSecret: secret
  baseUrl: http://application-example-host.com
```

| Name | Description | Default |
| --- | --- | --- |
| `managementUi.redis.database` | Redis database index for the management UI workload. | `1` |
| `managementUi.server.port` | Management UI container port. | `8080` |
| `managementUi.sso.clientApp.clientId` | OAuth2 client ID used by the management UI workload. | `vauthenticator-management-ui` |
| `managementUi.sso.clientApp.clientSecret` | OAuth2 client secret used by the management UI workload. | `secret` |
| `managementUi.baseUrl` | Public management UI base URL. | `http://application-example-host.com` |

### Management UI Documents And Assets

```yaml
managementUi:
  documentRepository:
    engine: s3
    bucketName: test
    fsBasePath: dist
    documentType:
      mail:
        cacheName: mail-document-local-cache
        cacheTtl: 1m
      staticAsset:
        cacheName: static-asset-document-local-cache
        cacheTtl: 1m
  assetServer:
    onS3:
      enabled: false
      bundleVersion: ""
    baseUrl: http://localhost:8080
```

| Name | Description | Default |
| --- | --- | --- |
| `managementUi.documentRepository.engine` | Document repository engine. | `s3` |
| `managementUi.documentRepository.bucketName` | S3 bucket when the S3 document engine is used. | `test` |
| `managementUi.documentRepository.fsBasePath` | Filesystem base path when filesystem documents are used. | `dist` |
| `managementUi.documentRepository.documentType.mail.*` | Mail document cache settings. | See `values.yaml` |
| `managementUi.documentRepository.documentType.staticAsset.*` | Static asset cache settings. | See `values.yaml` |
| `managementUi.assetServer.onS3.enabled` | Serve assets from S3. | `false` |
| `managementUi.assetServer.onS3.bundleVersion` | Optional S3 bundle version. | `""` |
| `managementUi.assetServer.baseUrl` | Asset server base URL. | `http://localhost:8080` |
