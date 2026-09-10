package br.com.jucelio.sentinelfraud.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class FraudDecisionConsumer {
    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedRepository;
    private final DeadLetterEventRepository deadLetterRepository;
    private final MeterRegistry metrics;

    public FraudDecisionConsumer(ObjectMapper objectMapper, ProcessedEventRepository processedRepository,
            DeadLetterEventRepository deadLetterRepository, MeterRegistry metrics) {
        this.objectMapper = objectMapper;
        this.processedRepository = processedRepository;
        this.deadLetterRepository = deadLetterRepository;
        this.metrics = metrics;
    }

    @KafkaListener(topics = "${kafka.topics.fraud-decisions}")
    public void consume(ConsumerRecord<String, String> record) {
        process(record.key(), record.value(), record.topic(), false);
    }

    @KafkaListener(topics = "${kafka.topics.fraud-decisions-replay}", groupId = "sentinelfraud-replay-v1")
    public void consumeReplay(ConsumerRecord<String, String> record) {
        process(record.key(), record.value(), record.topic(), true);
    }

    @Transactional
    public void process(String key, String payload, String sourceTopic, boolean replay) {
        if (processedRepository.existsByAggregateId(key)) {
            metrics.counter("fraud.kafka.consumer", "outcome", "duplicate").increment();
            return;
        }
        if (!replay && key.startsWith("tx-force-dlt-")) {
            metrics.counter("fraud.kafka.consumer", "outcome", "failure").increment();
            throw new IllegalStateException("Forced downstream failure for DLT demonstration");
        }
        try {
            var event = objectMapper.readTree(payload);
            processedRepository.saveAndFlush(ProcessedEventEntity.builder()
                    .id(UUID.randomUUID()).aggregateId(key).sourceTopic(sourceTopic)
                    .decision(event.path("decision").asText()).riskScore(event.path("riskScore").asInt())
                    .processedAt(Instant.now()).build());
            metrics.counter("fraud.kafka.consumer", "outcome", replay ? "replayed" : "processed").increment();
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid fraud decision event", ex);
        } catch (DataIntegrityViolationException ex) {
            metrics.counter("fraud.kafka.consumer", "outcome", "duplicate").increment();
        }
    }

    @KafkaListener(topics = "${kafka.topics.fraud-decisions-dlt}", groupId = "sentinelfraud-dlt-storage-v1")
    @Transactional
    public void consumeDeadLetter(ConsumerRecord<String, String> record) {
        if (deadLetterRepository.findByAggregateId(record.key()).isPresent()) return;
        deadLetterRepository.save(DeadLetterEventEntity.builder()
                .id(UUID.randomUUID()).aggregateId(record.key())
                .originalTopic("fraud.assessment.completed.v1").payload(record.value())
                .errorMessage("Consumer retries exhausted").status(DeadLetterStatus.PENDING)
                .createdAt(Instant.now()).build());
        metrics.counter("fraud.kafka.dlt", "outcome", "stored").increment();
    }
}
