# ADR-0005: Observability via Spring Boot Actuator

## Status

Accepted — 2026-04

## Context

Every production service needs:

- **Health checks** — for Docker/Kubernetes to know whether to restart or route traffic to the instance.
- **Metrics** — for alerting and capacity planning.
- **Runtime configurability** — changing a log level in prod without redeploying is often the difference between debugging an incident in minutes vs hours.
- **Build/version info** — for verifying which version of the service is actually running.

These can be built by hand. They can also be obtained almost for free from Spring Boot Actuator + Micrometer, which the Spring ecosystem has standardized on.

## Decision

I include `spring-boot-starter-actuator` and `micrometer-registry-prometheus` in the template, exposing:

| Endpoint                     | Purpose                                         |
|:-----------------------------|:------------------------------------------------|
| `/actuator/health/liveness`  | Docker/K8s liveness check — "is the JVM alive?" |
| `/actuator/health/readiness` | LB/K8s readiness check — "can it serve traffic?"|
| `/actuator/prometheus`       | Prometheus metrics scrape target                |
| `/actuator/loggers`          | Runtime log level inspection and change         |
| `/actuator/info`             | Build version, SHA, Spring Boot version         |

### Exposure

Actuator endpoints are **exposed on a separate management port** (e.g., 8081) in production. The public app port (8080) only serves business endpoints.

This separates concerns:

- Production load balancers route public traffic to 8080 only.
- Internal tooling (Prometheus, K8s probes, on-call tooling) can hit 8081 directly on the pod/container network.

Authentication on the management port is **not required in typical setups** because the port is not reachable from the public internet. If your deployment has no network segmentation, secure the management port with basic auth or a JWT.

### What I do NOT do

- **I do not expose an HTTP endpoint that serves log file contents.** Logs are shipped to a log aggregator (Loki, ELK, CloudWatch, Grafana Cloud, etc.). Reading logs through an HTTP endpoint on the service itself is an anti-pattern — logs are too large, often contain PII, and the aggregator is the correct tool.
- **I do not implement custom `/healthz` endpoints from scratch.** Actuator does this better with composable `HealthIndicator` beans. If I need to check specific dependencies (e.g., "is the database reachable", "is the OAuth provider responsive"), I add custom `HealthIndicator`s.

## Consequences

### Positive

- **Zero-code health checks.** Actuator's built-in `DataSourceHealthIndicator` pings the DB. `DiskSpaceHealthIndicator` checks disk. Contributing a dependency check is ~20 lines of code.
- **Prometheus-native.** The `/actuator/prometheus` endpoint is the expected Prometheus format. No conversion layer needed.
- **Runtime log-level control.** On-call engineers can bump `com.template.identity` to `DEBUG` via `POST /actuator/loggers/com.template.identity` during an incident, then back to `INFO` afterward. No redeploy, no restart.
- **Standard across services.** Every team using the template has the same endpoints at the same URLs. Dashboards, alerts, and runbooks can be written once and reused.

### Negative / Tradeoffs

- **Small dependency footprint.** Actuator adds ~5MB to the fat JAR. Negligible.
- **Port separation requires infrastructure config.** The management port (8081) must be configured in Docker/K8s but not exposed publicly. I document this in the deployment section of `README.md`.
- **Endpoints must be secured in networks without segmentation.** Explicitly called out — the assumption of "internal network only" breaks if you deploy straight to a VM with a public IP and no firewall.

## Alternatives Considered

### Custom health/metric endpoints from scratch

Rejected. Re-inventing what Actuator provides for free is pure cost. Custom `/healthz` that only returns 200 OK is not actually a health check — it doesn't verify the app can do its job (DB connection, disk space, dependencies).

### Externalize everything to the platform (K8s only)

Rejected. While Kubernetes can do DNS-based probes and external health checks, the app-aware checks (DB reachability, config loaded, migrations applied) can only come from inside the app. Actuator is the standard way to do this in Spring.

### Expose log file contents over HTTP

Explicitly rejected. Even with auth, this is the wrong tool. Log aggregators exist for this purpose and are vastly better at search, retention, and access control.
