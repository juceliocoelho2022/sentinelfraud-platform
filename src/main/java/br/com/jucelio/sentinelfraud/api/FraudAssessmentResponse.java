package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.domain.Decision;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FraudAssessmentResponse(UUID assessmentId, String transactionId, Decision decision,
                                      int riskScore, List<String> reasons, Instant assessedAt) { }
