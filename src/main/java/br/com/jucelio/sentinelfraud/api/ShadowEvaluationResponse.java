package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.domain.Decision;

import java.time.Instant;
import java.util.UUID;

public record ShadowEvaluationResponse(UUID id, String transactionId, String experimentVersion,
        Decision championDecision, Decision challengerDecision, Decision effectiveDecision,
        int riskScore, int rolloutBucket, boolean promoted, boolean diverged, Instant evaluatedAt) { }
