# VAuthenticator Chart Values

This document describes the values for the `charts/vauthenticator` Helm chart.

The chart currently renders:

- `application`: the VAuthenticator authorization server
- optional Redis dependency when `in-namespace.redis.enabled=true`
- optional PostgreSQL dependency when `in-namespace.db.enabled=true`; PostgreSQL is
  the only persistence, so a datasource is always required

The management UI is not part of this chart. It is distributed by the separate
`charts/management-ui` chart — see
[`charts/management-ui/README.md`](management-ui/README.md).

## Development Commands

Run these commands from `helm-charts`:

```bash
helm dependency update charts/vauthenticator
helm lint charts/vauthenticator --set ingress.host=localhost --set application.datasource.url=jdbc:postgresql://localhost:5432/
helm template vauthenticator charts/vauthenticator --set ingress.host=localhost --set application.datasource.url=jdbc:postgresql://localhost:5432/
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

## PostgreSQL Dependency

PostgreSQL is the only persistence (DynamoDB was removed in 0.10). The chart's
`spring.datasource` block (in the `application` ConfigMap) is always rendered and
requires `application.datasource.url` — rendering fails without it.

```yaml
in-namespace:
  db:
    enabled: true

postgresql:
  primary:
    initdb:
      scriptsConfigMap: vauthenticator-postgres-initdb

application:
  datasource:
    url: jdbc:postgresql://db.local.vauthenticator.com:5432/
    username: postgres
    password: postgres
```

| Name | Description | Default |
| --- | --- | --- |
| `in-namespace.db.enabled` | Install the Bitnami PostgreSQL dependency in the release namespace. | `true` |
| `postgresql.*` | Values passed to the Bitnami PostgreSQL subchart. | See `values.yaml` |
| `postgresql.primary.initdb.scriptsConfigMap` | Name of the ConfigMap the subchart mounts for `initdb` bootstrap scripts. Must match the ConfigMap the chart renders from `files/schema.sql`. | `vauthenticator-postgres-initdb` |
| `application.datasource.url` | JDBC URL for the Spring datasource. Required: rendering fails if unset. | `""` |
| `application.datasource.username` | Spring datasource username. | `""` |
| `application.datasource.password` | Spring datasource password. | `""` |

The chart ships `files/schema.sql`, the SQL schema used to bootstrap a fresh
PostgreSQL database (roles, groups, group-to-role mappings, etc.). It is rendered
into a `vauthenticator-postgres-initdb` ConfigMap and consumed by the PostgreSQL
subchart's `primary.initdb.scriptsConfigMap` on first startup.

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

These groups apply to the `application` workload.

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
  dnsConfig:
    ndots: "1"

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
  tag: "latest"

lables: {}
selectorLabels:
  app: vauthenticator
podAnnotations: {}
```

| Name | Description | Default |
| --- | --- | --- |
| `*.pod.probes.liveness.*` | Liveness probe timing. | `10`, `30` |
| `*.pod.probes.rediness.*` | Readiness probe timing. The value name is currently spelled `rediness` in the chart API. | `10`, `30` |
| `*.pod.dnsConfig.ndots` | Pod DNS `ndots` option. Kept low so external hostnames (e.g. AWS service endpoints) resolve as absolute names instead of first being tried against inherited search domains, which can misresolve on networks whose upstream resolver answers unknown names under its search domain instead of returning NXDOMAIN. | `"1"` |
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
| `*.image.tag` | Container image tag. | `latest` |
| `*.lables` | Extra pod labels. The value name is currently spelled `lables` in the chart API. | `{}` |
| `*.selectorLabels` | Selector labels used by Deployment and Service. Change with care. | workload-specific |
| `*.podAnnotations` | Extra pod annotations. | `{}` |

## Gateway API (Gateway + HTTPRoute)

Alternative to `ingress.enabled` for routing to the `application` workload via the
Kubernetes Gateway API. `gateway.enabled` renders a `Gateway` resource named after
the release; `httpRoute.enabled` renders an `HTTPRoute` bound to that `Gateway` via
`parentRefs`. Enable both together. `ingress.enabled` and `httpRoute.enabled` are
mutually exclusive — the chart fails to render if both are `true`.

```yaml
gateway:
  enabled: false

httpRoute:
  enabled: false
  gatewayClassName: nginx
  gatewayPort: 443
  hostnames: []
  tls: []
  annotations: {}
  rules: []
```

| Name | Description | Default |
| --- | --- | --- |
| `gateway.enabled` | Render the `Gateway` resource. | `false` |
| `httpRoute.enabled` | Render the `HTTPRoute` resource. Fails if `ingress.enabled` is also `true`. | `false` |
| `httpRoute.gatewayClassName` | `Gateway` spec `gatewayClassName`. | `nginx` |
| `httpRoute.gatewayPort` | Listener port on the `Gateway`. | `443` |
| `httpRoute.hostnames` | Hostnames for both the `Gateway` HTTPS listener and the `HTTPRoute`. | `[]` |
| `httpRoute.tls` | `certificateRefs` list (`- secretName: ...`) for the `Gateway`'s TLS listener. | `[]` |
| `httpRoute.annotations` | Extra `HTTPRoute` annotations. | `{}` |
| `httpRoute.rules` | `HTTPRoute` rule `matches` list; `backendRefs` is always the `application` Service on `application.server.port`. | `[]` |

## Authorization Server

```yaml
application:
  sessionTimeout: 24h
  profiles: kms
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
| `application.profiles` | Active Spring profiles: `kms` for AWS KMS key management, empty for plain-Java key management. | `kms` |
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
    sns:
      endpointOverride:
    lambda:
      endpointOverride:
```

Use these values for LocalStack or non-default AWS endpoints.

### Lambda Token Enhancement

```yaml
application:
  lambda:
    aws:
      enabled: false
      functionName: vauthenticator-token-enhancer
      functionResultCacheTtl: 10s
```

| Name | Description | Default |
| --- | --- | --- |
| `application.lambda.aws.enabled` | Enable AWS Lambda-based token customization. | `false` |
| `application.lambda.aws.functionName` | Lambda function name invoked for token enhancement. | `vauthenticator-token-enhancer` |
| `application.lambda.aws.functionResultCacheTtl` | Cache TTL for Lambda invocation results, as a Spring duration. | `10s` |

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
    lifeCycle:
      cron: "0 0 * * * *"
```

| Key | Description | Default |
| --- | --- | --- |
| `application.password.lifeCycle.cron` | Spring cron expression (six fields, seconds first) for the password lifecycle job. | `"0 0 * * * *"` |

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

### Roles and Caches

```yaml
application:
  role:
    protectedFromDelete: ROLE_USER,VAUTHENTICATOR_ADMIN
  cache:
    account:
      enabled: false
      ttl: 1h
      name: account_cache
    role:
      enabled: false
      ttl: 1h
      name: role_cache
    clientApplication:
      enabled: false
      ttl: 1h
      name: client_application_cache
```

| Name | Description | Default |
| --- | --- | --- |
| `application.role.protectedFromDelete` | Comma-separated roles that cannot be deleted. | `ROLE_USER,VAUTHENTICATOR_ADMIN` |
| `application.cache.account.enabled` | Read accounts through a Redis cache. Every account write evicts it. | `false` |
| `application.cache.account.ttl` | Account cache TTL, as a Spring duration. | `1h` |
| `application.cache.account.name` | Account cache region name. | `account_cache` |
| `application.cache.role.enabled` | Read roles through a Redis cache. | `false` |
| `application.cache.role.ttl` | Role cache TTL, as a Spring duration. | `1h` |
| `application.cache.role.name` | Role cache region name. | `role_cache` |
| `application.cache.clientApplication.enabled` | Read client applications through a Redis cache. | `false` |
| `application.cache.clientApplication.ttl` | Client application cache TTL, as a Spring duration. | `1h` |
| `application.cache.clientApplication.name` | Client application cache region name. | `client_application_cache` |

### Tickets

A ticket is the one-time proof behind MFA enrollment, reset-password links and email-verification links. It is deleted when redeemed and cannot be redeemed after its TTL.

```yaml
application:
  ticket:
    ttl:
      mfaEnrollment: 5m
      resetPassword: 15m
      emailVerification: 24h
```

| Key | Description | Default |
| --- | --- | --- |
| `application.ticket.ttl.mfaEnrollment` | How long an MFA enrollment ticket can be associated, as a Spring duration. | `5m` |
| `application.ticket.ttl.resetPassword` | How long a reset-password link works, as a Spring duration. | `15m` |
| `application.ticket.ttl.emailVerification` | How long an email-verification link works, as a Spring duration. | `24h` |

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
