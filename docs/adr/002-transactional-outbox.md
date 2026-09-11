# ADR-002: usar Transactional Outbox

- Status: Aceita
- Data: 2026-09-11

## Contexto

Salvar uma decisão no PostgreSQL e publicar diretamente no Kafka cria dual write: o banco pode confirmar enquanto a publicação falha, ou o evento pode ser publicado antes de uma reversão no banco.

## Decisão

Persistir decisão e evento de Outbox na mesma transação. Um relay separado faz claim concorrente com `SKIP LOCKED`, publica no Kafka e controla retry, backoff e estado terminal `DEAD`.

## Consequências

- Elimina a janela de inconsistência do dual write.
- Entrega é pelo menos uma vez; consumidores devem ser idempotentes.
- Existe atraso pequeno entre commit e publicação.
- A tabela de Outbox precisa de retenção e monitoramento operacional.
