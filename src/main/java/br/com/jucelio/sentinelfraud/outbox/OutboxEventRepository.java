package br.com.jucelio.sentinelfraud.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {
    @Query(value = """
            SELECT * FROM outbox_events
             WHERE (status = 'PENDING' AND next_attempt_at <= CURRENT_TIMESTAMP)
                OR (status = 'PROCESSING' AND claimed_at < CURRENT_TIMESTAMP - INTERVAL '1 minute')
             ORDER BY created_at
             LIMIT :batchSize
             FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEventEntity> findClaimable(@Param("batchSize") int batchSize);
}
