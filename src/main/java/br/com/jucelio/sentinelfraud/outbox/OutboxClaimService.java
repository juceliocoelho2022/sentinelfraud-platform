package br.com.jucelio.sentinelfraud.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxClaimService {
    private final OutboxEventRepository repository;

    public OutboxClaimService(OutboxEventRepository repository) { this.repository = repository; }

    @Transactional
    public List<ClaimedOutboxEvent> claim(int batchSize) {
        Instant now = Instant.now();
        return repository.findClaimable(batchSize).stream().map(event -> {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setAttempts(event.getAttempts() + 1);
            event.setClaimedAt(now);
            event.setLastError(null);
            return new ClaimedOutboxEvent(event.getId(), event.getAggregateId(), event.getTopic(),
                    event.getPayload(), event.getAttempts());
        }).toList();
    }

    @Transactional
    public void markPublished(UUID id) {
        repository.findById(id).ifPresent(event -> {
            event.setStatus(OutboxStatus.PUBLISHED);
            event.setPublishedAt(Instant.now());
            event.setClaimedAt(null);
            event.setLastError(null);
        });
    }

    @Transactional
    public void markFailed(UUID id, int attempts, int maxAttempts, String error) {
        repository.findById(id).ifPresent(event -> {
            event.setStatus(attempts >= maxAttempts ? OutboxStatus.DEAD : OutboxStatus.PENDING);
            event.setNextAttemptAt(Instant.now().plusSeconds(Math.min(60, 1L << Math.min(attempts, 6))));
            event.setClaimedAt(null);
            event.setLastError(truncate(error));
        });
    }

    private String truncate(String error) {
        if (error == null) return "Unknown Kafka publication error";
        return error.length() <= 1000 ? error : error.substring(0, 1000);
    }

    public record ClaimedOutboxEvent(UUID id, String aggregateId, String topic, String payload, int attempts) {}
}
