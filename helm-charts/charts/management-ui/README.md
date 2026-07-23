# management-ui Chart Values

This document describes the values for the `charts/management-ui` Helm chart.

The chart deploys the VAuthenticator management UI (a static React SPA) as a `ClusterIP`
Service and Deployment, with an optional Ingress and an optional Gateway API `HTTPRoute`
for external access. The image is built from the `management-ui` project's `Dockerfile`:
an nginx container serving the built SPA, which also proxies `GET /api/config` to the
`config-manager` service so the browser fetches runtime configuration same-origin instead
of needing to know where `config-manager` actually lives. See `management-ui/AGENTS.md`
and `management-ui/docker/default.conf.template` for the serving contract this chart
configures.

## Development Commands

Run these commands from `helm-charts`:

```bash
helm lint charts/management-ui --set application.configManagerUpstream=http://localhost:8086
helm template management-ui charts/management-ui --set application.configManagerUpstream=http://localhost:8086
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
  configManagerUpstream: http://config-manager-example-host.com:8086
```

| Name | Environment variable | Description | Default |
| --- | --- | --- | --- |
| `application.configManagerUpstream` | `CONFIG_MANAGER_UPSTREAM` | Scheme + host + port nginx proxies `GET /api/config` to. Required, no safe default — point it at the `config-manager` chart's Service for this release, e.g. `http://<config-manager-release-name>.<namespace>.svc.cluster.local:8086`. | placeholder |

This is required by the Deployment template (`required` fails `helm template`/`helm
install` with a clear error instead of deploying a pod that can't reach config-manager).

nginx resolves this host at request time rather than once at container startup (the
Deployment always sets `NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1`), so the pod doesn't
crash-loop if `config-manager`'s Service isn't resolvable yet when it starts, and it
picks up Service IP changes without a restart.
