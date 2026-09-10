package br.com.jucelio.sentinelfraud.kafka;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeadLetterEventEntity {
    @Id private UUID id;
    @Column(name = "aggregate_id", nullable = false, unique = true) private String aggregateId;
    @Column(name = "original_topic", nullable = false) private String originalTopic;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(name = "error_message", nullable = false) private String errorMessage;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DeadLetterStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "replayed_at") private Instant replayedAt;
}
