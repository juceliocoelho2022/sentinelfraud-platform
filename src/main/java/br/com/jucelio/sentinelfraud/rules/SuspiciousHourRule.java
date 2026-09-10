package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.time.ZoneOffset;

@Component @Order(30)
public class SuspiciousHourRule implements FraudRule {
    public RuleResult evaluate(TransactionContext tx) {
        int hour = tx.occurredAt().atZone(ZoneOffset.UTC).getHour();
        return hour < 5 ? RuleResult.hit(20, "UNUSUAL_HOUR_UTC") : RuleResult.pass();
    }
    public String code() { return "FR003"; }
}
