package br.com.jucelio.sentinelfraud.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public record FraudAssessmentRequest(
        @NotBlank String transactionId,
        @NotBlank String customerId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min=3,max=3) String currency,
        @NotBlank String deviceId,
        @NotBlank String ipAddress,
        @NotBlank @Size(min=2,max=2) String country,
        @NotNull Instant occurredAt) { }
