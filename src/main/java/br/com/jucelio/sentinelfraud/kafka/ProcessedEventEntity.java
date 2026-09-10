package br.com.jucelio.sentinelfraud.kafka;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedEventEntity {
    @Id private UUID id;
    @Column(name = "aggregate_id", nullable = false, unique = true) private String aggregateId;
    @Column(name = "source_topic", nullable = false) private String sourceTopic;
    @Column(nullable = false) private String decision;
    @Column(name = "risk_score", nullable = false) private int riskScore;
    @Column(name = "processed_at", nullable = false) private Instant processedAt;
}
