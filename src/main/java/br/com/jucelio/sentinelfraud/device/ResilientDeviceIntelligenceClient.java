package br.com.jucelio.sentinelfraud.device;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ResilientDeviceIntelligenceClient implements DeviceIntelligenceClient {
    private final RestClient restClient;
    private final MeterRegistry metrics;

    public ResilientDeviceIntelligenceClient(
            @Qualifier("deviceIntelligenceRestClient") RestClient restClient,
            MeterRegistry metrics) {
        this.restClient = restClient;
        this.metrics = metrics;
    }

    @Override
    @Retry(name = "deviceIntelligence")
    @CircuitBreaker(name = "deviceIntelligence", fallbackMethod = "fallback")
    public DeviceIntelligenceResult analyze(String deviceId, String ipAddress) {
        var result = restClient.get()
                .uri(uri -> uri.path("/internal/device-intelligence/{deviceId}")
                        .queryParam("ipAddress", ipAddress).build(deviceId))
                .retrieve()
                .body(DeviceIntelligenceResult.class);
        if (result == null) {
            throw new IllegalStateException("Device intelligence returned an empty response");
        }
        metrics.counter("fraud.device.intelligence", "outcome", "success",
                "risk", result.riskLevel().name()).increment();
        return result;
    }

    DeviceIntelligenceResult fallback(String deviceId, String ipAddress, Throwable error) {
        metrics.counter("fraud.device.intelligence", "outcome", "fallback").increment();
        return DeviceIntelligenceResult.unavailable(deviceId);
    }
}
