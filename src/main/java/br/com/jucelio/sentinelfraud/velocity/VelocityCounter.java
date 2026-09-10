package br.com.jucelio.sentinelfraud.velocity;

import java.time.Duration;

public interface VelocityCounter {
    long recordAndCount(String scope, String value, String transactionId, Duration window);
}
