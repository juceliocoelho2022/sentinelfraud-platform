package br.com.jucelio.sentinelfraud.experiment;

import br.com.jucelio.sentinelfraud.domain.Decision;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChallengerEvaluatorTest {

    @Test
    void shouldPersistAndMeasureDivergenceWithoutChangingChampion() {
        var repository = mock(ShadowEvaluationRepository.class);
        var metrics = new SimpleMeterRegistry();
        var evaluator = new ChallengerEvaluator(repository, metrics, true, "challenger-v1", 35, 65);

        evaluator.evaluate("tx-shadow-1", 65, Decision.REVIEW);

        var captor = ArgumentCaptor.forClass(ShadowEvaluationEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getChampionDecision()).isEqualTo(Decision.REVIEW);
        assertThat(captor.getValue().getChallengerDecision()).isEqualTo(Decision.BLOCK);
        assertThat(captor.getValue().isDiverged()).isTrue();
        assertThat(metrics.get("fraud.challenger.comparison").counter().count()).isEqualTo(1);
    }

    @Test
    void shouldDoNothingWhenFeatureFlagIsDisabled() {
        var repository = mock(ShadowEvaluationRepository.class);
        var evaluator = new ChallengerEvaluator(repository, new SimpleMeterRegistry(),
                false, "challenger-v1", 35, 65);

        evaluator.evaluate("tx-shadow-2", 65, Decision.REVIEW);

        verifyNoInteractions(repository);
    }
}
