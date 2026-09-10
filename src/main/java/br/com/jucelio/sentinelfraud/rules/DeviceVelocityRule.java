package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import br.com.jucelio.sentinelfraud.velocity.VelocityCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Order(50)
public class DeviceVelocityRule extends AbstractVelocityRule {
    public DeviceVelocityRule(VelocityCounter counter,
                              @Value("${fraud.rules.velocity.window:PT1M}") Duration window,
                              @Value("${fraud.rules.velocity.threshold:5}") int threshold,
                              @Value("${fraud.rules.velocity.score:35}") int score) {
        super(counter, window, threshold, score, "device", "DEVICE_VELOCITY", TransactionContext::deviceId);
    }

    public String code() { return "FR005"; }
}
