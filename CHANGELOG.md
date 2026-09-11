# Changelog

Todas as mudanças relevantes do projeto são documentadas neste arquivo.

## 1.0.0 — 2026-09-11

- API antifraude com decisões explicáveis e idempotência.
- Regras estáticas, velocity checks em Redis e Device Intelligence resiliente.
- Persistência PostgreSQL versionada com Flyway.
- Transactional Outbox e Kafka com retry, DLT e replay.
- JWT RS256 e RBAC para ANALYST e ADMIN.
- Prometheus, Grafana, Tempo e tracing OpenTelemetry.
- Champion/challenger, shadow mode e rollout canário determinístico.
- Testes unitários, integração PostgreSQL com Testcontainers e cobertura JaCoCo.
- Architecture Decision Records para as decisões críticas.

## Histórico

- 0.9.0 — rollout canário e rollback.
- 0.8.0 — champion/challenger em shadow mode.
- 0.7.x — observabilidade e dashboard operacional.
- 0.6.0 — segurança JWT e RBAC.
- 0.5.x — consumidor Kafka, DLT e replay.
- 0.4.0 — Device Intelligence e Resilience4j.
- 0.3.0 — Transactional Outbox.
- 0.2.0 — velocity checks.
- 0.1.0 — MVP.
