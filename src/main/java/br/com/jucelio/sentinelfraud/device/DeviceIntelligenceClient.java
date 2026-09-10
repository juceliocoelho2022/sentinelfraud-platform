package br.com.jucelio.sentinelfraud.device;

public interface DeviceIntelligenceClient {
    DeviceIntelligenceResult analyze(String deviceId, String ipAddress);
}
