package br.com.jucelio.sentinelfraud.persistence;

import br.com.jucelio.sentinelfraud.domain.Decision;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="fraud_assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudAssessmentEntity {
    @Id private UUID id;
    @Column(name="transaction_id",nullable=false,unique=true) private String transactionId;
    @Column(name="customer_id",nullable=false) private String customerId;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Decision decision;
    @Column(name="risk_score",nullable=false) private int riskScore;
    @Column(name="reason_codes",nullable=false) private String reasonCodes;
    @Column(name="assessed_at",nullable=false) private Instant assessedAt;
}
