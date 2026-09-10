package br.com.jucelio.sentinelfraud.service;

import br.com.jucelio.sentinelfraud.api.FraudAssessmentRequest;
import br.com.jucelio.sentinelfraud.domain.Decision;
import br.com.jucelio.sentinelfraud.persistence.*;
import br.com.jucelio.sentinelfraud.rules.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class FraudDecisionServiceTest {
    FraudAssessmentRepository repository=mock(FraudAssessmentRepository.class);
    OutboxService outbox=mock(OutboxService.class);
    FraudDecisionService service;
    @BeforeEach void setup() {
        when(repository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(i->i.getArgument(0));
        service=new FraudDecisionService(List.of(new HighAmountRule(new BigDecimal("10000")),new ForeignTransactionRule()),repository,outbox,new SimpleMeterRegistry());
    }
    @Test void shouldBlockCombinedRisk() {
        var req=new FraudAssessmentRequest("tx-99","c-1",new BigDecimal("12000"),"BRL","dev","10.0.0.1","US",Instant.parse("2026-09-09T12:00:00Z"));
        var result=service.assess(req);
        assertThat(result.decision()).isEqualTo(Decision.BLOCK);
        assertThat(result.riskScore()).isEqualTo(70);
        verify(outbox).enqueue(eq("fraud.assessment.completed.v1"),eq("tx-99"),any());
    }
}
