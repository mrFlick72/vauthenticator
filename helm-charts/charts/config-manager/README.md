# config-manager Chart Values

This document describes the values for the `charts/config-manager` Helm chart.

The chart deploys the `config-manager` Go service as a `ClusterIP` Service and Deployment,
with an optional Ingress and an optional Gateway API `HTTPRoute` for external access. See
`config-manager/AGENTS.md` for the runtime contract this chart configures.

## Development Commands

Run these commands from `helm-charts`:

```bash
helm lint charts/config-manager --set application.managementUiServerUrl=http://localhost
helm template config-manager charts/config-manager --set application.managementUiServerUrl=http://localhost
```

## Image, Service, Resources

```yaml
image:
  repository: mrflick72/vauthenticator-config-manager-k8s
  pullPolicy: Always
  tag: "latest"

service:
  type: ClusterIP
  port: 8086

resources:
  requests:
    cpu: "50m"
    memory: "32Mi"
  limits:
    cpu: "100m"
    memory: "64Mi"

replicaCount: 1
```

| Name | Description | Default |
| --- | --- | --- |
| `image.repository` | Container image repository. | `mrflick72/vauthenticator-config-manager-k8s` |
| `image.pullPolicy` | Container image pull policy. | `Always` |
| `image.tag` | Container image tag. | `latest` |
| `service.type` | Kubernetes service type. | `ClusterIP` |
| `service.port` | Service and container port. Matches the app's `SERVER_ADDRESS` port. | `8086` |
| `resources` | Container resource requests and limits. | See `values.yaml` |
| `replicaCount` | Deployment replica count. | `1` |
| `pod.probes.liveness.*` / `pod.probes.readiness.*` | Probe timing. Both probes call the unauthenticated `GET /api/config` endpoint. | `5`, `30` |
| `labels` | Extra pod labels. | `{}` |
| `selectorLabels` | Selector labels used by Deployment and Service. Change with care. | `app: vauthenticator-config-manager` |
| `podAnnotations` | Extra pod annotations. | `{}` |
| `imagePullSecrets` | Image pull secrets. | `[]` |

## Ingress

```yaml
ingress:
  host: "*"
  annotations: {}
  tls: {}
  enabled: true
  class: nginx
```

| Name | Description | Default |
| --- | --- | --- |
| `ingress.enabled` | Render an Ingress resource. | `true` |
| `ingress.host` | Ingress host. | `"*"` |
| `ingress.annotations` | Extra ingress annotations. | `{}` |
| `ingress.tls` | Ingress TLS block. | `{}` |
| `ingress.class` | Ingress class annotation value. | `nginx` |

The rendered Ingress always routes path `/` to this Service. Since config-manager only
ever serves `/api/config`, give it its own `ingress.host` rather than sharing the
management UI's host, unless your ingress controller resolves overlapping host/path rules
across separate Ingress objects the way you expect.

## Gateway API HTTPRoute

```yaml
gateway:
  enabled: false
  annotations: {}
  labels: {}
  parentRefs: []
  hostnames: []
  rules: []
```

| Name | Description | Default |
| --- | --- | --- |
| `gateway.enabled` | Render a Gateway API `HTTPRoute` instead of / alongside the Ingress. | `false` |
| `gateway.parentRefs` | Gateway references. Required (fails the render) when `gateway.enabled=true`. | `[]` |
| `gateway.hostnames` | Hostnames for the route. | `[]` |
| `gateway.rules` | Route rules. Defaults to a single `PathPrefix /` rule targeting this Service when empty. | `[]` |
| `gateway.annotations` / `gateway.labels` | Extra metadata on the `HTTPRoute`. | `{}` |

TLS/cert-manager is configured on the referenced Gateway listener, not on this `HTTPRoute`.

## Application Configuration

```yaml
application:
  serverAddress: ":8086"
  managementUiServerUrl: http://management-ui-example-host.com
  idpBaseUrl: http://application-example-host.com
  clientApplicationId: vauthenticator-management-ui
  redirectUri: http://management-ui-example-host.com/callback
  authenticationCheckInterval: "15000"
  apiBaseUrl: http://application-example-host.com/api
```

These map 1:1 to the environment variables read by config-manager (`config-manager/internal/config/config.go`):

| Name | Environment variable | Description | Default |
| --- | --- | --- | --- |
| `application.serverAddress` | `SERVER_ADDRESS` | Listen address. | `:8086` |
| `application.managementUiServerUrl` | `MANAGEMENT_UI_SERVER_URL` | Allowed CORS origin; must match the management UI host. Required, no safe default — always set explicitly. | placeholder |
| `application.idpBaseUrl` | `IDP_BASE_URL` | Authorization server base URL returned to the management UI. | placeholder |
| `application.clientApplicationId` | `CLIENT_APPLICATION_ID` | OAuth2 client ID returned to the management UI. | `vauthenticator-management-ui` |
| `application.redirectUri` | `REDIRECT_URI` | OAuth2 redirect URI returned to the management UI. | placeholder |
| `application.authenticationCheckInterval` | `AUTHENTICATION_CHECK_INTERVAL` | Auth check polling interval in milliseconds, as a string. | `15000` |
| `application.apiBaseUrl` | `API_BASE_URL` | Authorization server API base URL returned to the management UI. | placeholder |

All of these except `serverAddress` are required by the application at startup (see
`Config.Validate()`); the Deployment template uses `required` so a missing value fails
`helm template`/`helm install` with a clear error instead of deploying a broken pod.
