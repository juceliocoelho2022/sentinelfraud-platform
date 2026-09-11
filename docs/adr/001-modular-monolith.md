# ADR-001: iniciar como monólito modular

- Status: Aceita
- Data: 2026-09-11

## Contexto

O domínio exige API, regras antifraude, persistência, mensageria, segurança e observabilidade. Separar cada capacidade em um microsserviço desde o início aumentaria custo operacional e pontos de falha antes de existir demanda comprovada de escala independente.

## Decisão

Iniciar como monólito modular, com pacotes e dependências organizados por responsabilidade. As fronteiras de regras, device intelligence, outbox, Kafka, experimentação e persistência permanecem explícitas.

## Consequências

- Deploy, testes e desenvolvimento local mais simples.
- Transações ACID podem cobrir a decisão e a Outbox.
- Módulos com escala ou ciclo de mudança próprios podem ser extraídos futuramente.
- É necessário vigiar acoplamento entre módulos para evitar um monólito desorganizado.
