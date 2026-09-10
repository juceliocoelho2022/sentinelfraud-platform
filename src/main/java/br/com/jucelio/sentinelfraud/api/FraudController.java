package br.com.jucelio.sentinelfraud.api;

import br.com.jucelio.sentinelfraud.service.FraudDecisionService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/fraud-assessments")
public class FraudController {
    private final FraudDecisionService service;
    public FraudController(FraudDecisionService service) { this.service=service; }
    @PostMapping public ResponseEntity<FraudAssessmentResponse> assess(@Valid @RequestBody FraudAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.assess(request));
    }
    @GetMapping("/{transactionId}") public FraudAssessmentResponse find(@PathVariable String transactionId) {
        return service.find(transactionId);
    }
}
