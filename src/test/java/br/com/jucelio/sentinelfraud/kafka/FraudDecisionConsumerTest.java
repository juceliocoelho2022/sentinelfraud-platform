package br.com.jucelio.sentinelfraud.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FraudDecisionConsumerTest {
    private final ProcessedEventRepository processed = mock(ProcessedEventRepository.class);
    private final DeadLetterEventRepository deadLetters = mock(DeadLetterEventRepository.class);
    private FraudDecisionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new FraudDecisionConsumer(new ObjectMapper(), processed, deadLetters,
                new SimpleMeterRegistry());
    }

    @Test
    void shouldProcessValidEvent() {
        consumer.process("tx-1", "{\"decision\":\"BLOCK\",\"riskScore\":80}", "fraud.topic", false);

        verify(processed).saveAndFlush(argThat(event -> event.getAggregateId().equals("tx-1")
                && event.getDecision().equals("BLOCK") && event.getRiskScore() == 80));
    }

    @Test
    void shouldIgnoreDuplicate() {
        when(processed.existsByAggregateId("tx-1")).thenReturn(true);

        consumer.process("tx-1", "{}", "fraud.topic", false);

        verify(processed, never()).saveAndFlush(any());
    }

    @Test
    void shouldForceFailureOnlyOnOriginalTopic() {
        var payload = "{\"decision\":\"REVIEW\",\"riskScore\":45}";
        assertThatThrownBy(() -> consumer.process("tx-force-dlt-1", payload, "fraud.topic", false))
                .isInstanceOf(IllegalStateException.class);

        consumer.process("tx-force-dlt-1", payload, "fraud.topic.replay", true);
        verify(processed).saveAndFlush(any());
    }

    @Test
    void shouldStoreDeadLetterIdempotently() {
        when(deadLetters.findByAggregateId("tx-failed")).thenReturn(Optional.empty());

        consumer.consumeDeadLetter(new ConsumerRecord<>("fraud.topic.DLT", 0, 1,
                "tx-failed", "{\"decision\":\"BLOCK\"}"));

        verify(deadLetters).save(argThat(event -> event.getAggregateId().equals("tx-failed")
                && event.getStatus() == DeadLetterStatus.PENDING));
    }
}
