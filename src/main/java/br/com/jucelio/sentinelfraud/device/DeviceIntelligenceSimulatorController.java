package br.com.jucelio.sentinelfraud.device;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/internal/device-intelligence")
public class DeviceIntelligenceSimulatorController {

    @GetMapping("/{deviceId}")
    DeviceIntelligenceResult analyze(@PathVariable String deviceId,
                                     @RequestParam String ipAddress) throws InterruptedException {
        if (deviceId.startsWith("error-")) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Provider failure simulation");
        }
        if (deviceId.startsWith("slow-")) {
            Thread.sleep(1_500);
        }
        if (deviceId.startsWith("risk-")) {
            return new DeviceIntelligenceResult(deviceId, DeviceRiskLevel.HIGH, 97,
                    List.of("EMULATOR", "ROOTED_DEVICE", "IP_MISMATCH"), false);
        }
        if (deviceId.startsWith("review-")) {
            return new DeviceIntelligenceResult(deviceId, DeviceRiskLevel.MEDIUM, 75,
                    List.of("NEW_DEVICE"), false);
        }
        return new DeviceIntelligenceResult(deviceId, DeviceRiskLevel.LOW, 92, List.of(), false);
    }
}
