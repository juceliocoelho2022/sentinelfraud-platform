package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.outbox.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void enqueue(String topic, String aggregateId, Object event) {
        try {
            Instant now = Instant.now();
            repository.save(OutboxEventEntity.builder()
                    .id(UUID.randomUUID()).aggregateId(aggregateId).topic(topic)
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxStatus.PENDING).attempts(0)
                    .nextAttemptAt(now).createdAt(now).build());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize outbox event", ex);
        }
    }
}
