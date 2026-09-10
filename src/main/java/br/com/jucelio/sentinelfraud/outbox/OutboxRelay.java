package br.com.jucelio.sentinelfraud.outbox;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxRelay {
    private final OutboxClaimService claimService;
    private final KafkaTemplate<String, String> kafka;
    private final MeterRegistry metrics;
    private final int batchSize;
    private final int maxAttempts;
    private final Duration sendTimeout;

    public OutboxRelay(OutboxClaimService claimService, KafkaTemplate<String, String> kafka,
                       MeterRegistry metrics,
                       @Value("${outbox.relay.batch-size:50}") int batchSize,
                       @Value("${outbox.relay.max-attempts:8}") int maxAttempts,
                       @Value("${outbox.relay.send-timeout:PT5S}") Duration sendTimeout) {
        this.claimService = claimService;
        this.kafka = kafka;
        this.metrics = metrics;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.sendTimeout = sendTimeout;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay:1000}")
    public void publishPendingEvents() {
        for (var event : claimService.claim(batchSize)) {
            try {
                kafka.send(event.topic(), event.aggregateId(), event.payload())
                        .get(sendTimeout.toMillis(), TimeUnit.MILLISECONDS);
                claimService.markPublished(event.id());
                metrics.counter("outbox.events", "status", "published").increment();
            } catch (Exception ex) {
                claimService.markFailed(event.id(), event.attempts(), maxAttempts, rootMessage(ex));
                metrics.counter("outbox.events", "status", "failed").increment();
            }
        }
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getMessage();
    }
}
