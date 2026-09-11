package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.experiment.ShadowEvaluationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/experiments")
public class ExperimentAdminController {
    private final ShadowEvaluationRepository repository;

    public ExperimentAdminController(ShadowEvaluationRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/shadow-evaluations")
    public List<ShadowEvaluationResponse> list() {
        return repository.findTop100ByOrderByEvaluatedAtDesc().stream()
                .map(e -> new ShadowEvaluationResponse(e.getId(), e.getTransactionId(), e.getExperimentVersion(),
                        e.getChampionDecision(), e.getChallengerDecision(), e.getEffectiveDecision(),
                        e.getRiskScore(), e.getRolloutBucket(), e.isPromoted(),
                        e.isDiverged(), e.getEvaluatedAt()))
                .toList();
    }
}
