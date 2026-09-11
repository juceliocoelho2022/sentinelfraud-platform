package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.api.*;
import br.com.jucelio.sentinelfraud.domain.*;
import br.com.jucelio.sentinelfraud.experiment.ChallengerEvaluator;
import br.com.jucelio.sentinelfraud.persistence.*;
import br.com.jucelio.sentinelfraud.rules.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class FraudDecisionService {
    private final List<FraudRule> rules;
    private final FraudAssessmentRepository repository;
    private final OutboxService outbox;
    private final MeterRegistry metrics;
    private final ChallengerEvaluator challenger;

    public FraudDecisionService(List<FraudRule> rules, FraudAssessmentRepository repository,
                                OutboxService outbox, MeterRegistry metrics, ChallengerEvaluator challenger) {
        this.rules=List.copyOf(rules); this.repository=repository; this.outbox=outbox; this.metrics=metrics;
        this.challenger=challenger;
    }

    @Transactional
    public FraudAssessmentResponse assess(FraudAssessmentRequest request) {
        var previous=repository.findByTransactionId(request.transactionId());
        if (previous.isPresent()) { metrics.counter("fraud.idempotency.hit").increment(); return response(previous.get()); }
        var tx=new TransactionContext(request.transactionId(),request.customerId(),request.amount(),
                request.currency().toUpperCase(),request.deviceId(),request.ipAddress(),
                request.country().toUpperCase(),request.occurredAt());
        var results=rules.stream().map(rule -> rule.evaluate(tx)).filter(RuleResult::matched).toList();
        int score=Math.min(100,results.stream().mapToInt(RuleResult::score).sum());
        Decision decision=score>=70?Decision.BLOCK:score>=40?Decision.REVIEW:Decision.APPROVE;
        var entity=FraudAssessmentEntity.builder().id(UUID.randomUUID()).transactionId(tx.transactionId())
                .customerId(tx.customerId()).decision(decision).riskScore(score)
                .reasonCodes(results.stream().map(RuleResult::reason).reduce((a,b)->a+","+b).orElse("NONE"))
                .assessedAt(Instant.now()).build();
        try { repository.saveAndFlush(entity); }
        catch (DataIntegrityViolationException ex) { return repository.findByTransactionId(tx.transactionId()).map(this::response).orElseThrow(() -> ex); }
        metrics.counter("fraud.decisions", "decision", decision.name()).increment();
        challenger.evaluate(tx.transactionId(), score, decision);
        outbox.enqueue("fraud.assessment.completed.v1", tx.transactionId(), response(entity));
        return response(entity);
    }

    @Transactional(readOnly=true)
    public FraudAssessmentResponse find(String transactionId) {
        return repository.findByTransactionId(transactionId).map(this::response)
                .orElseThrow(() -> new AssessmentNotFoundException(transactionId));
    }

    private FraudAssessmentResponse response(FraudAssessmentEntity e) {
        return new FraudAssessmentResponse(e.getId(),e.getTransactionId(),e.getDecision(),e.getRiskScore(),
                Arrays.asList(e.getReasonCodes().split(",")),e.getAssessedAt());
    }
}
