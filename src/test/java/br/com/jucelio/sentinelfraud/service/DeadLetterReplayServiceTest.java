package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.kafka.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeadLetterReplayServiceTest {
    private final DeadLetterEventRepository repository = mock(DeadLetterEventRepository.class);
    private final OutboxService outbox = mock(OutboxService.class);
    private final DeadLetterReplayService service = new DeadLetterReplayService(repository, outbox,
            new ObjectMapper(), new SimpleMeterRegistry());

    @Test
    void shouldReplayThroughTransactionalOutbox() {
        var event = event(DeadLetterStatus.PENDING);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        var response = service.replay(event.getId());

        assertThat(response.status()).isEqualTo(DeadLetterStatus.REPLAYED);
        assertThat(response.replayedAt()).isNotNull();
        verify(outbox).enqueue(eq("fraud.assessment.completed.v1.replay"), eq("tx-failed"), any());
    }

    @Test
    void shouldNotPublishSameReplayTwice() {
        var event = event(DeadLetterStatus.REPLAYED);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        service.replay(event.getId());

        verifyNoInteractions(outbox);
    }

    private DeadLetterEventEntity event(DeadLetterStatus status) {
        return DeadLetterEventEntity.builder().id(UUID.randomUUID()).aggregateId("tx-failed")
                .originalTopic("fraud.assessment.completed.v1")
                .payload("{\"decision\":\"BLOCK\",\"riskScore\":80}")
                .errorMessage("Retries exhausted").status(status).createdAt(Instant.now()).build();
    }
}
