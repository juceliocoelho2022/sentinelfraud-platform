# ADR-004: champion/challenger com rollout determinístico

- Status: Aceita
- Data: 2026-09-11

## Contexto

Alterações em limiares antifraude podem aumentar falsos positivos ou perdas. Uma troca integral não permite medir divergência com segurança nem reduzir rapidamente a exposição.

## Decisão

Executar primeiro o challenger em shadow mode. A promoção usa um percentual e um bucket determinístico derivado da `transactionId`. A auditoria registra decisões champion, challenger e efetiva, bucket, promoção e divergência.

## Consequências

- Permite comparar políticas sem impacto quando o rollout é zero.
- A mesma transação permanece no mesmo grupo durante retries.
- O rollout pode avançar gradualmente e voltar imediatamente para zero.
- Métricas de fraude confirmada e falso positivo ainda são necessárias antes de promover uma política permanentemente.
