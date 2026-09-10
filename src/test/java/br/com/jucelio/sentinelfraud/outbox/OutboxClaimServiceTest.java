package br.com.jucelio.sentinelfraud.outbox;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxClaimServiceTest {
    private final OutboxEventRepository repository = mock(OutboxEventRepository.class);
    private final OutboxClaimService service = new OutboxClaimService(repository);

    @Test
    void shouldClaimPendingEvent() {
        var event = event(OutboxStatus.PENDING, 0);
        when(repository.findClaimable(10)).thenReturn(List.of(event));

        var claimed = service.claim(10);

        assertThat(claimed).hasSize(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getClaimedAt()).isNotNull();
    }

    @Test
    void shouldMarkEventPublished() {
        var event = event(OutboxStatus.PROCESSING, 1);
        when(repository.findById(event.getId())).thenReturn(Optional.of(event));

        service.markPublished(event.getId());

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldRetryAndEventuallyMarkDead() {
        var retry = event(OutboxStatus.PROCESSING, 2);
        when(repository.findById(retry.getId())).thenReturn(Optional.of(retry));
        service.markFailed(retry.getId(), 2, 8, "Kafka unavailable");
        assertThat(retry.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(retry.getLastError()).isEqualTo("Kafka unavailable");

        var dead = event(OutboxStatus.PROCESSING, 8);
        when(repository.findById(dead.getId())).thenReturn(Optional.of(dead));
        service.markFailed(dead.getId(), 8, 8, "Kafka unavailable");
        assertThat(dead.getStatus()).isEqualTo(OutboxStatus.DEAD);
    }

    private OutboxEventEntity event(OutboxStatus status, int attempts) {
        return OutboxEventEntity.builder().id(UUID.randomUUID()).aggregateId("tx-1")
                .topic("fraud.topic").payload("{}").status(status).attempts(attempts)
                .nextAttemptAt(Instant.now()).createdAt(Instant.now()).build();
    }
}
