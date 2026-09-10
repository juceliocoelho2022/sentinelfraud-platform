package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component @Order(10)
public class HighAmountRule implements FraudRule {
    private final BigDecimal threshold;
    public HighAmountRule(@Value("${fraud.rules.high-amount-threshold:10000}") BigDecimal threshold) { this.threshold = threshold; }
    public RuleResult evaluate(TransactionContext tx) {
        return tx.amount().compareTo(threshold) >= 0 ? RuleResult.hit(45, "HIGH_AMOUNT") : RuleResult.pass();
    }
    public String code() { return "FR001"; }
}
