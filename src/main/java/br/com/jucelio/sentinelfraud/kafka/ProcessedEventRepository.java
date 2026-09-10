package br.com.jucelio.sentinelfraud.kafka;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {
    boolean existsByAggregateId(String aggregateId);
}
