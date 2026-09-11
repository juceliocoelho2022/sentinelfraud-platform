# Architecture Decision Records

Este diretório registra decisões arquiteturais relevantes do SentinelFraud Platform.

| ADR | Decisão | Status |
|---|---|---|
| [ADR-001](001-modular-monolith.md) | Monólito modular como ponto de partida | Aceita |
| [ADR-002](002-transactional-outbox.md) | Transactional Outbox para publicação Kafka | Aceita |
| [ADR-003](003-redis-velocity-check.md) | Redis e Lua para velocity checks | Aceita |
| [ADR-004](004-champion-challenger-rollout.md) | Champion/challenger com rollout determinístico | Aceita |

ADRs não são documentação de implementação. Eles preservam contexto, alternativas e consequências para que decisões possam ser revisadas com evidências.
