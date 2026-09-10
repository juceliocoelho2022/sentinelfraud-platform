package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.device.*;
import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceIntelligenceRuleTest {
    private final TransactionContext tx = new TransactionContext("tx-1", "customer-1",
            BigDecimal.TEN, "BRL", "device-1", "203.0.113.1", "BR", Instant.now());

    @Test
    void shouldScoreHighRiskDevice() {
        DeviceIntelligenceClient client = (device, ip) -> new DeviceIntelligenceResult(
                device, DeviceRiskLevel.HIGH, 97, List.of("ROOTED_DEVICE"), false);

        var result = new DeviceIntelligenceRule(client, 15).evaluate(tx);

        assertThat(result).isEqualTo(RuleResult.hit(60, "HIGH_RISK_DEVICE"));
    }

    @Test
    void shouldApplyConservativeScoreWhenProviderIsUnavailable() {
        DeviceIntelligenceClient client = (device, ip) -> DeviceIntelligenceResult.unavailable(device);

        var result = new DeviceIntelligenceRule(client, 15).evaluate(tx);

        assertThat(result).isEqualTo(RuleResult.hit(15, "DEVICE_INTELLIGENCE_UNAVAILABLE"));
    }

    @Test
    void shouldPassLowRiskDevice() {
        DeviceIntelligenceClient client = (device, ip) -> new DeviceIntelligenceResult(
                device, DeviceRiskLevel.LOW, 92, List.of(), false);

        assertThat(new DeviceIntelligenceRule(client, 15).evaluate(tx).matched()).isFalse();
    }
}
