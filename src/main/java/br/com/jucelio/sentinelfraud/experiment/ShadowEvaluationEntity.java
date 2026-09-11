package br.com.jucelio.sentinelfraud.experiment;

import br.com.jucelio.sentinelfraud.domain.Decision;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fraud_shadow_evaluations",
        uniqueConstraints = @UniqueConstraint(name = "uk_shadow_transaction_version",
                columnNames = {"transaction_id", "experiment_version"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ShadowEvaluationEntity {
    @Id private UUID id;
    @Column(name = "transaction_id", nullable = false) private String transactionId;
    @Column(name = "experiment_version", nullable = false) private String experimentVersion;
    @Enumerated(EnumType.STRING) @Column(name = "champion_decision", nullable = false) private Decision championDecision;
    @Enumerated(EnumType.STRING) @Column(name = "challenger_decision", nullable = false) private Decision challengerDecision;
    @Enumerated(EnumType.STRING) @Column(name = "effective_decision", nullable = false) private Decision effectiveDecision;
    @Column(name = "risk_score", nullable = false) private int riskScore;
    @Column(name = "rollout_bucket", nullable = false) private int rolloutBucket;
    @Column(nullable = false) private boolean promoted;
    @Column(nullable = false) private boolean diverged;
    @Column(name = "evaluated_at", nullable = false) private Instant evaluatedAt;
}
