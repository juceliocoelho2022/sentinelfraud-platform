package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;

public interface FraudRule {
    RuleResult evaluate(TransactionContext transaction);
    String code();
}
