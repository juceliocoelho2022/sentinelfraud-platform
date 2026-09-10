package br.com.jucelio.sentinelfraud.outbox;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OutboxEventEntity {
    @Id private UUID id;
    @Column(name = "aggregate_id", nullable = false) private String aggregateId;
    @Column(nullable = false) private String topic;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private OutboxStatus status;
    @Column(nullable = false) private int attempts;
    @Column(name = "next_attempt_at", nullable = false) private Instant nextAttemptAt;
    @Column(name = "claimed_at") private Instant claimedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "last_error") private String lastError;
}
