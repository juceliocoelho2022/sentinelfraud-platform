package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.api.DeadLetterResponse;
import br.com.jucelio.sentinelfraud.kafka.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeadLetterReplayService {
    private final DeadLetterEventRepository repository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    private final MeterRegistry metrics;

    public DeadLetterReplayService(DeadLetterEventRepository repository, OutboxService outboxService,
            ObjectMapper objectMapper, MeterRegistry metrics) {
        this.repository = repository;
        this.outboxService = outboxService;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true)
    public List<DeadLetterResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::response).toList();
    }

    @Transactional
    public DeadLetterResponse replay(UUID id) {
        var event = repository.findById(id)
                .orElseThrow(() -> new AssessmentNotFoundException("dead-letter:" + id));
        if (event.getStatus() == DeadLetterStatus.REPLAYED) return response(event);
        try {
            outboxService.enqueue("fraud.assessment.completed.v1.replay", event.getAggregateId(),
                    objectMapper.readTree(event.getPayload()));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid dead-letter payload", ex);
        }
        event.setStatus(DeadLetterStatus.REPLAYED);
        event.setReplayedAt(Instant.now());
        metrics.counter("fraud.kafka.dlt", "outcome", "replay_requested").increment();
        return response(event);
    }

    private DeadLetterResponse response(DeadLetterEventEntity event) {
        return new DeadLetterResponse(event.getId(), event.getAggregateId(), event.getOriginalTopic(),
                event.getStatus(), event.getErrorMessage(), event.getCreatedAt(), event.getReplayedAt());
    }
}
