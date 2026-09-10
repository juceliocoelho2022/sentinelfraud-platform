package br.com.jucelio.sentinelfraud.outbox;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;

class OutboxRelayTest {
    @Test
    void shouldPublishAndMarkEvent() {
        var claims = mock(OutboxClaimService.class);
        @SuppressWarnings("unchecked") var kafka = (KafkaTemplate<String, String>) mock(KafkaTemplate.class);
        var id = UUID.randomUUID();
        var event = new OutboxClaimService.ClaimedOutboxEvent(id, "tx-1", "fraud.topic", "{}", 1);
        when(claims.claim(10)).thenReturn(List.of(event));
        when(kafka.send("fraud.topic", "tx-1", "{}")).thenReturn(CompletableFuture.completedFuture(null));

        new OutboxRelay(claims, kafka, new SimpleMeterRegistry(), 10, 8, Duration.ofSeconds(1))
                .publishPendingEvents();

        verify(claims).markPublished(id);
        verify(claims, never()).markFailed(any(), anyInt(), anyInt(), any());
    }

    @Test
    void shouldScheduleRetryWhenKafkaFails() {
        var claims = mock(OutboxClaimService.class);
        @SuppressWarnings("unchecked") var kafka = (KafkaTemplate<String, String>) mock(KafkaTemplate.class);
        var id = UUID.randomUUID();
        var event = new OutboxClaimService.ClaimedOutboxEvent(id, "tx-2", "fraud.topic", "{}", 2);
        when(claims.claim(10)).thenReturn(List.of(event));
        when(kafka.send("fraud.topic", "tx-2", "{}")).thenReturn(CompletableFuture.failedFuture(new RuntimeException("offline")));

        new OutboxRelay(claims, kafka, new SimpleMeterRegistry(), 10, 8, Duration.ofSeconds(1))
                .publishPendingEvents();

        verify(claims).markFailed(id, 2, 8, "offline");
    }
}
