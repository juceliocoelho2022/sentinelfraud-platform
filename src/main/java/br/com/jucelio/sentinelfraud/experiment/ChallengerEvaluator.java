package br.com.jucelio.sentinelfraud.experiment;

import br.com.jucelio.sentinelfraud.domain.Decision;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ChallengerEvaluator {
    private final ShadowEvaluationRepository repository;
    private final MeterRegistry metrics;
    private final boolean enabled;
    private final String version;
    private final int reviewThreshold;
    private final int blockThreshold;

    public ChallengerEvaluator(ShadowEvaluationRepository repository, MeterRegistry metrics,
            @Value("${fraud.experiments.challenger.enabled:false}") boolean enabled,
            @Value("${fraud.experiments.challenger.version:challenger-v1}") String version,
            @Value("${fraud.experiments.challenger.review-threshold:35}") int reviewThreshold,
            @Value("${fraud.experiments.challenger.block-threshold:65}") int blockThreshold) {
        if (reviewThreshold < 0 || blockThreshold > 100 || reviewThreshold >= blockThreshold) {
            throw new IllegalArgumentException("Challenger thresholds must satisfy 0 <= review < block <= 100");
        }
        this.repository = repository;
        this.metrics = metrics;
        this.enabled = enabled;
        this.version = version;
        this.reviewThreshold = reviewThreshold;
        this.blockThreshold = blockThreshold;
    }

    public void evaluate(String transactionId, int riskScore, Decision championDecision) {
        if (!enabled) return;

        Decision challengerDecision = decide(riskScore);
        boolean diverged = challengerDecision != championDecision;
        repository.save(ShadowEvaluationEntity.builder()
                .id(UUID.randomUUID())
                .transactionId(transactionId)
                .experimentVersion(version)
                .championDecision(championDecision)
                .challengerDecision(challengerDecision)
                .riskScore(riskScore)
                .diverged(diverged)
                .evaluatedAt(Instant.now())
                .build());
        metrics.counter("fraud.challenger.comparison",
                "version", version,
                "champion", championDecision.name(),
                "challenger", challengerDecision.name(),
                "diverged", Boolean.toString(diverged)).increment();
    }

    private Decision decide(int score) {
        return score >= blockThreshold ? Decision.BLOCK
                : score >= reviewThreshold ? Decision.REVIEW : Decision.APPROVE;
    }
}
