package br.com.jucelio.sentinelfraud.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FraudAssessmentRepository extends JpaRepository<FraudAssessmentEntity, UUID> {
    Optional<FraudAssessmentEntity> findByTransactionId(String transactionId);
}
