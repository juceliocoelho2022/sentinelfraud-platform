package br.com.jucelio.sentinelfraud.experiment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShadowEvaluationRepository extends JpaRepository<ShadowEvaluationEntity, UUID> {
    List<ShadowEvaluationEntity> findTop100ByOrderByEvaluatedAtDesc();
}
