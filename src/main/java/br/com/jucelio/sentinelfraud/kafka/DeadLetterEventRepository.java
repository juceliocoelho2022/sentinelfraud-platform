package br.com.jucelio.sentinelfraud.kafka;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEventEntity, UUID> {
    Optional<DeadLetterEventEntity> findByAggregateId(String aggregateId);
    List<DeadLetterEventEntity> findAllByOrderByCreatedAtDesc();
}
