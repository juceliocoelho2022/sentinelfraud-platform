package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component @Order(20)
public class ForeignTransactionRule implements FraudRule {
    public RuleResult evaluate(TransactionContext tx) {
        return "BR".equalsIgnoreCase(tx.country()) ? RuleResult.pass() : RuleResult.hit(25, "FOREIGN_COUNTRY");
    }
    public String code() { return "FR002"; }
}
