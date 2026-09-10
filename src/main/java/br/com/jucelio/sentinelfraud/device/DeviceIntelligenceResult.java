package br.com.jucelio.sentinelfraud.device;

import java.util.List;

public record DeviceIntelligenceResult(
        String deviceId,
        DeviceRiskLevel riskLevel,
        int confidence,
        List<String> signals,
        boolean fallback
) {
    public DeviceIntelligenceResult {
        signals = signals == null ? List.of() : List.copyOf(signals);
    }

    public static DeviceIntelligenceResult unavailable(String deviceId) {
        return new DeviceIntelligenceResult(deviceId, DeviceRiskLevel.UNKNOWN, 0,
                List.of("PROVIDER_UNAVAILABLE"), true);
    }
}
