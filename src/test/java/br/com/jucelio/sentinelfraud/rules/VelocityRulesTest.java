package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import br.com.jucelio.sentinelfraud.velocity.VelocityCounter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VelocityRulesTest {
    private final VelocityCounter counter = mock(VelocityCounter.class);
    private final TransactionContext tx = new TransactionContext("tx-5", "customer-1", BigDecimal.TEN,
            "BRL", "device-1", "203.0.113.10", "BR", Instant.parse("2026-09-10T12:00:00Z"));

    @Test
    void shouldFlagCustomerAtThreshold() {
        when(counter.recordAndCount("customer", "customer-1", "tx-5", Duration.ofMinutes(1))).thenReturn(5L);
        var result = new CustomerVelocityRule(counter, Duration.ofMinutes(1), 5, 35).evaluate(tx);
        assertThat(result).isEqualTo(RuleResult.hit(35, "CUSTOMER_VELOCITY"));
    }

    @Test
    void shouldPassDeviceBelowThreshold() {
        when(counter.recordAndCount("device", "device-1", "tx-5", Duration.ofMinutes(1))).thenReturn(4L);
        var result = new DeviceVelocityRule(counter, Duration.ofMinutes(1), 5, 35).evaluate(tx);
        assertThat(result.matched()).isFalse();
    }

    @Test
    void shouldFlagIpAtThreshold() {
        when(counter.recordAndCount("ip", "203.0.113.10", "tx-5", Duration.ofMinutes(1))).thenReturn(5L);
        var result = new IpVelocityRule(counter, Duration.ofMinutes(1), 5, 35).evaluate(tx);
        assertThat(result.reason()).isEqualTo("IP_VELOCITY");
    }
}
