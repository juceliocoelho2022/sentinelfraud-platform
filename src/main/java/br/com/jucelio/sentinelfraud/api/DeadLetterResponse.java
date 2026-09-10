package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.kafka.DeadLetterStatus;
import java.time.Instant;
import java.util.UUID;

public record DeadLetterResponse(UUID id, String aggregateId, String originalTopic,
                                 DeadLetterStatus status, String errorMessage,
                                 Instant createdAt, Instant replayedAt) { }
