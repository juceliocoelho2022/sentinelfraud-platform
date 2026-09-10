package br.com.jucelio.sentinelfraud.device;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceIntelligenceSimulatorControllerTest {
    private final DeviceIntelligenceSimulatorController controller = new DeviceIntelligenceSimulatorController();

    @Test
    void shouldSimulateRiskProfiles() throws InterruptedException {
        assertThat(controller.analyze("risk-rooted", "203.0.113.1").riskLevel())
                .isEqualTo(DeviceRiskLevel.HIGH);
        assertThat(controller.analyze("review-new", "203.0.113.1").riskLevel())
                .isEqualTo(DeviceRiskLevel.MEDIUM);
        assertThat(controller.analyze("device-known", "203.0.113.1").riskLevel())
                .isEqualTo(DeviceRiskLevel.LOW);
    }

    @Test
    void shouldSimulateProviderFailure() {
        assertThatThrownBy(() -> controller.analyze("error-provider", "203.0.113.1"))
                .isInstanceOf(ResponseStatusException.class);
    }
}
