# management-ui Chart Values

This document describes the values for the `charts/management-ui` Helm chart.

The chart deploys the VAuthenticator management UI (a static React SPA) as a `ClusterIP`
Service and Deployment, with an optional Ingress and an optional Gateway API `HTTPRoute`
for external access. The image is built from the `management-ui` project's `Dockerfile`:
an nginx container serving the built SPA, plus a `GET /config.json` static file generated
at container start from this chart's `application.*` values (see "Application
Configuration" below). See `management-ui/AGENTS.md` and
`management-ui/docker/default.conf.template` for the serving contract this chart
configures.

## Development Commands

Run these commands from `helm-charts`:

```bash
helm lint charts/management-ui \
  --set application.idpBaseUrl=http://localhost:9090 \
  --set application.clientApplicationId=vauthenticator-management-ui \
  --set application.redirectUri=http://localhost:8085/callback \
  --set application.authenticationCheckInterval=15000 \
  --set application.apiBaseUrl=http://localhost:9090/api
helm template management-ui charts/management-ui \
  --set application.idpBaseUrl=http://localhost:9090 \
  --set application.clientApplicationId=vauthenticator-management-ui \
  --set application.redirectUri=http://localhost:8085/callback \
  --set application.authenticationCheckInterval=15000 \
  --set application.apiBaseUrl=http://localhost:9090/api
```

## Image, Service, Resources

```yaml
image:
  repository: mrflick72/vauthenticator-management-ui-k8s
  pullPolicy: Always
  tag: "latest"

service:
  type: ClusterIP
  port: 80

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
| `image.repository` | Container image repository. | `mrflick72/vauthenticator-management-ui-k8s` |
| `image.pullPolicy` | Container image pull policy. | `Always` |
| `image.tag` | Container image tag. | `latest` |
| `service.type` | Kubernetes service type. | `ClusterIP` |
| `service.port` | Service and container port (nginx listens on 80). | `80` |
| `resources` | Container resource requests and limits. | See `values.yaml` |
| `replicaCount` | Deployment replica count. | `1` |
| `pod.probes.liveness.*` / `pod.probes.readiness.*` | Probe timing. Both probes call `GET /logout.html` since a static server has no dedicated health endpoint; any built SPA entry point that returns `200` works. | `5`, `30` |
| `labels` | Extra pod labels. | `{}` |
| `selectorLabels` | Selector labels used by Deployment and Service. Change with care. | `app: vauthenticator-management-ui` |
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

The rendered Ingress always routes path `/` to this Service.

## Gateway API (Gateway + HTTPRoute)

Alternative to `ingress.enabled` for routing to this Service via the Kubernetes Gateway
API, mirroring the pattern used by the `vauthenticator` chart's `application` workload.
`gateway.enabled` renders a `Gateway` resource named after the release; `httpRoute.enabled`
renders an `HTTPRoute` bound to that `Gateway` via `parentRefs`. Enable both together.
`ingress.enabled` and `httpRoute.enabled` are mutually exclusive — the chart fails to
render if both are `true`.

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
| `httpRoute.rules` | `HTTPRoute` rule `matches` list; `backendRefs` is always this Service on `service.port`. No default rule is rendered when empty — set this explicitly when `httpRoute.enabled=true`. | `[]` |

## Application Configuration

```yaml
application:
  idpBaseUrl: http://application-example-host.com
  clientApplicationId: vauthenticator-management-ui
  redirectUri: http://management-ui-example-host.com/callback
  authenticationCheckInterval: "15000"
  apiBaseUrl: http://application-example-host.com/api
```

| Name | Environment variable | Description | Default |
| --- | --- | --- | --- |
| `application.idpBaseUrl` | `IDP_BASE_URL` | Base URL of the authorization server. | placeholder |
| `application.clientApplicationId` | `CLIENT_APPLICATION_ID` | OAuth2 client ID the SPA authenticates as. | placeholder |
| `application.redirectUri` | `REDIRECT_URI` | OAuth2 authorization code redirect URI, must match the client app's registered callback. | placeholder |
| `application.authenticationCheckInterval` | `AUTHENTICATION_CHECK_INTERVAL` | Milliseconds between OIDC session-management checks. | `"15000"` |
| `application.apiBaseUrl` | `API_BASE_URL` | Base URL the admin SPA calls for management API requests. | placeholder |

This chart renders these into a `ConfigMap` (`templates/configmap.yaml`), wired into the
Deployment via `envFrom`. All five are required by the ConfigMap template (`required`
fails `helm template`/`helm install` with a clear error instead of deploying a pod that
serves broken config).

nginx's entrypoint script (`management-ui/docker/40-generate-app-config.sh`) reads these
env vars at container start and renders `management-ui/docker/config.json.template` into
`/usr/share/nginx/html/config.json`, served at `GET /config.json` with
`Cache-Control: no-store`.
