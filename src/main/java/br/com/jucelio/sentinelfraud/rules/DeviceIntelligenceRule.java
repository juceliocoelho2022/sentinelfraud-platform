package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.device.DeviceIntelligenceClient;
import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(15)
public class DeviceIntelligenceRule implements FraudRule {
    private final DeviceIntelligenceClient client;
    private final int unavailableScore;

    public DeviceIntelligenceRule(DeviceIntelligenceClient client,
            @Value("${fraud.device-intelligence.unavailable-score:15}") int unavailableScore) {
        this.client = client;
        this.unavailableScore = unavailableScore;
    }

    @Override
    public RuleResult evaluate(TransactionContext tx) {
        var result = client.analyze(tx.deviceId(), tx.ipAddress());
        return switch (result.riskLevel()) {
            case HIGH -> RuleResult.hit(60, "HIGH_RISK_DEVICE");
            case MEDIUM -> RuleResult.hit(30, "MEDIUM_RISK_DEVICE");
            case UNKNOWN -> RuleResult.hit(unavailableScore, "DEVICE_INTELLIGENCE_UNAVAILABLE");
            case LOW -> RuleResult.pass();
        };
    }

    @Override
    public String code() {
        return "FR007";
    }
}
