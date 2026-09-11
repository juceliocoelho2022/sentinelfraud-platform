# ADR-003: usar Redis e Lua nos velocity checks

- Status: Aceita
- Data: 2026-09-11

## Contexto

Regras de velocidade precisam contar eventos recentes por cliente, dispositivo e IP com baixa latência. Uma sequência comum de remover, adicionar, contar e expirar chaves estaria sujeita a condições de corrida.

## Decisão

Usar Sorted Sets do Redis e executar a janela móvel em um script Lua atômico. Identificadores são transformados com SHA-256 antes de compor chaves.

## Consequências

- Baixa latência e operação atômica.
- Reduz exposição de identificadores nas chaves.
- Redis torna-se dependência do caminho de decisão.
- Timeout, degradação e capacidade devem ser observados.
