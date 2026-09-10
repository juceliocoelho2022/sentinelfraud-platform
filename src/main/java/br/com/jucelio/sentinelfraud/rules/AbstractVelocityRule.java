package br.com.jucelio.sentinelfraud.rules;

import br.com.jucelio.sentinelfraud.domain.TransactionContext;
import br.com.jucelio.sentinelfraud.velocity.VelocityCounter;

import java.time.Duration;
import java.util.function.Function;

abstract class AbstractVelocityRule implements FraudRule {
    private final VelocityCounter counter;
    private final Duration window;
    private final int threshold;
    private final int score;
    private final String scope;
    private final String reason;
    private final Function<TransactionContext, String> valueExtractor;

    AbstractVelocityRule(VelocityCounter counter, Duration window, int threshold, int score,
                         String scope, String reason, Function<TransactionContext, String> valueExtractor) {
        this.counter = counter;
        this.window = window;
        this.threshold = threshold;
        this.score = score;
        this.scope = scope;
        this.reason = reason;
        this.valueExtractor = valueExtractor;
    }

    @Override
    public RuleResult evaluate(TransactionContext tx) {
        long count = counter.recordAndCount(scope, valueExtractor.apply(tx), tx.transactionId(), window);
        return count >= threshold ? RuleResult.hit(score, reason) : RuleResult.pass();
    }
}
