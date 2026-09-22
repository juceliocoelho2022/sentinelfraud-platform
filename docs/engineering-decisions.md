# Engineering Decisions — SentinelFraud

O SentinelFraud já possui ADRs detalhados em docs/adr. Este arquivo funciona como mapa executivo das decisões, conectando arquitetura, risco, qualidade e operação.

## 1. Objetivo

Uma decisão antifraude precisa ser rápida, explicável, auditável e resistente a falhas. O sistema também precisa evitar duplicidade e permitir evolução controlada de regras.

## 2. Transactional Outbox

**Problema:** persistir a decisão e publicar o evento em operações separadas cria risco de inconsistência.

**Decisão:** decisão e evento são gravados de forma transacional; um relay publica depois.

**Trade-off:** mais estado operacional e necessidade de monitorar backlog/retry, em troca de maior confiabilidade.

## 3. Idempotency

Retries de cliente, timeout ou redelivery não podem gerar múltiplas decisões divergentes para a mesma transação. O transactionId funciona como identidade lógica da avaliação.

## 4. Velocity Check com Redis + Lua

Sorted Sets + Lua permitem janela móvel com operação atômica e baixa latência.

**Trade-off:** Redis vira dependência operacional e precisa de timeout, fallback e observabilidade.

## 5. Resilience4j

Timeout, retry e circuit breaker protegem a integração de Device Intelligence.

Retry é aplicado apenas a falhas potencialmente transitórias e com limite explícito. Circuit breaker evita pressionar continuamente uma dependência degradada.

## 6. DLT + Replay

Mensagens que não podem ser processadas após retry são isoladas para inspeção. Após corrigir a causa raiz, o replay deve ocorrer de forma controlada e idempotente.

## 7. Champion / Challenger

Uma nova política é primeiro executada em shadow mode. O rollout determinístico permite promoção gradual e rollback rápido.

Isso reduz risco ao comparar decisões antes de expor todo o tráfego à nova regra.

## 8. Qualidade

A estratégia combina:
- testes unitários;
- integração com PostgreSQL via Testcontainers;
- migrations Flyway;
- JaCoCo com quality gate;
- CI;
- ADRs para registrar decisões arquiteturais.

## 9. Observabilidade

Perguntas que o sistema deve responder:
- p95 está dentro do limite?
- Redis está degradado?
- a Outbox está acumulando?
- existe crescimento de DLT?
- challenger diverge do champion?
- circuit breaker está abrindo?

## 10. Investigação de incidente

Para aumento de latência:
1. validar health;
2. observar p95/p99;
3. correlacionar traces;
4. identificar dependência dominante;
5. verificar timeouts/retries/circuit breaker;
6. checar pool e backlog;
7. corrigir causa;
8. monitorar recuperação.

Novas decisões que alterem confiabilidade, dados, segurança ou operação devem gerar ADR.
