package br.com.jucelio.sentinelfraud.experiment;

import br.com.jucelio.sentinelfraud.domain.Decision;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ChallengerEvaluatorTest {

    @Test
    void shouldKeepChampionInShadowMode() {
        var repository = mock(ShadowEvaluationRepository.class);
        var evaluator = new ChallengerEvaluator(repository, new SimpleMeterRegistry(),
                true, "challenger-v2", 35, 65, 0);

        Decision effective = evaluator.evaluate("tx-shadow", 65, Decision.REVIEW);

        var captor = ArgumentCaptor.forClass(ShadowEvaluationEntity.class);
        verify(repository).save(captor.capture());
        assertThat(effective).isEqualTo(Decision.REVIEW);
        assertThat(captor.getValue().getChallengerDecision()).isEqualTo(Decision.BLOCK);
        assertThat(captor.getValue().isPromoted()).isFalse();
        assertThat(captor.getValue().isDiverged()).isTrue();
    }

    @Test
    void shouldPromoteChallengerAtFullRollout() {
        var repository = mock(ShadowEvaluationRepository.class);
        var metrics = new SimpleMeterRegistry();
        var evaluator = new ChallengerEvaluator(repository, metrics,
                true, "challenger-v2", 35, 65, 100);

        Decision effective = evaluator.evaluate("tx-canary", 65, Decision.REVIEW);

        assertThat(effective).isEqualTo(Decision.BLOCK);
        assertThat(metrics.get("fraud.challenger.comparison")
                .tag("promoted", "true").counter().count()).isEqualTo(1);
    }

    @Test
    void shouldReturnChampionWhenDisabled() {
        var repository = mock(ShadowEvaluationRepository.class);
        var evaluator = new ChallengerEvaluator(repository, new SimpleMeterRegistry(),
                false, "challenger-v2", 35, 65, 100);

        assertThat(evaluator.evaluate("tx-disabled", 65, Decision.REVIEW)).isEqualTo(Decision.REVIEW);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectInvalidConfiguration() {
        assertThatThrownBy(() -> new ChallengerEvaluator(mock(ShadowEvaluationRepository.class),
                new SimpleMeterRegistry(), true, "invalid", 65, 65, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ChallengerEvaluator(mock(ShadowEvaluationRepository.class),
                new SimpleMeterRegistry(), true, "invalid", 35, 65, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
