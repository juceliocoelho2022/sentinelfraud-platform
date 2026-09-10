package br.com.jucelio.sentinelfraud.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionContext(String transactionId, String customerId, BigDecimal amount,
                                 String currency, String deviceId, String ipAddress,
                                 String country, Instant occurredAt) { }
