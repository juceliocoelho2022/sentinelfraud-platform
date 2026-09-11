package br.com.jucelio.sentinelfraud.integration;

import br.com.jucelio.sentinelfraud.domain.Decision;
import br.com.jucelio.sentinelfraud.experiment.ShadowEvaluationEntity;
import br.com.jucelio.sentinelfraud.experiment.ShadowEvaluationRepository;
import br.com.jucelio.sentinelfraud.persistence.FraudAssessmentEntity;
import br.com.jucelio.sentinelfraud.persistence.FraudAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PersistenceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("sentinelfraud")
                    .withUsername("sentinel")
                    .withPassword("sentinel");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    FraudAssessmentRepository assessmentRepository;

    @Autowired
    ShadowEvaluationRepository shadowRepository;

    @Test
    void shouldApplyFlywaySchemaAndPersistOfficialAndCanaryDecisions() {
        Instant now = Instant.now();
        var assessment = FraudAssessmentEntity.builder()
                .id(UUID.randomUUID())
                .transactionId("tx-integration-1")
                .customerId("customer-integration")
                .decision(Decision.BLOCK)
                .riskScore(65)
                .reasonCodes("HIGH_AMOUNT,UNUSUAL_HOUR_UTC")
                .assessedAt(now)
                .build();

        assessmentRepository.saveAndFlush(assessment);

        var shadow = ShadowEvaluationEntity.builder()
                .id(UUID.randomUUID())
                .transactionId("tx-integration-1")
                .experimentVersion("challenger-integration")
                .championDecision(Decision.REVIEW)
                .challengerDecision(Decision.BLOCK)
                .effectiveDecision(Decision.BLOCK)
                .riskScore(65)
                .rolloutBucket(42)
                .promoted(true)
                .diverged(true)
                .evaluatedAt(now)
                .build();

        shadowRepository.saveAndFlush(shadow);

        assertThat(assessmentRepository.findByTransactionId("tx-integration-1"))
                .get().extracting(FraudAssessmentEntity::getDecision)
                .isEqualTo(Decision.BLOCK);

        assertThat(shadowRepository.findTop100ByOrderByEvaluatedAtDesc())
                .singleElement()
                .satisfies(saved -> {
                    assertThat(saved.getChampionDecision()).isEqualTo(Decision.REVIEW);
                    assertThat(saved.getChallengerDecision()).isEqualTo(Decision.BLOCK);
                    assertThat(saved.getEffectiveDecision()).isEqualTo(Decision.BLOCK);
                    assertThat(saved.getRolloutBucket()).isEqualTo(42);
                    assertThat(saved.isPromoted()).isTrue();
                    assertThat(saved.isDiverged()).isTrue();
                });
    }
}
