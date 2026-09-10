package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.outbox.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutboxServiceTest {
    @Test
    void shouldPersistPendingSerializedEvent() {
        var repository = mock(OutboxEventRepository.class);
        var service = new OutboxService(repository, new ObjectMapper());

        service.enqueue("fraud.topic", "tx-1", Map.of("decision", "BLOCK"));

        var captor = org.mockito.ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(captor.getValue().getAggregateId()).isEqualTo("tx-1");
        assertThat(captor.getValue().getPayload()).contains("BLOCK");
        assertThat(captor.getValue().getAttempts()).isZero();
    }
}
