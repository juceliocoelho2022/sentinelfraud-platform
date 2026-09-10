package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import br.com.jucelio.sentinelfraud.velocity.VelocityCounter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Order(40)
public class CustomerVelocityRule extends AbstractVelocityRule {
    public CustomerVelocityRule(VelocityCounter counter,
                                @Value("${fraud.rules.velocity.window:PT1M}") Duration window,
                                @Value("${fraud.rules.velocity.threshold:5}") int threshold,
                                @Value("${fraud.rules.velocity.score:35}") int score) {
        super(counter, window, threshold, score, "customer", "CUSTOMER_VELOCITY", TransactionContext::customerId);
    }

    public String code() { return "FR004"; }
}
