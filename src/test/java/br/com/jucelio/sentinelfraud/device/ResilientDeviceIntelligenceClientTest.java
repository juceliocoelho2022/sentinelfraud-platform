package br.com.jucelio.sentinelfraud.device;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ResilientDeviceIntelligenceClientTest {

    @Test
    void shouldReturnProviderAssessmentAndRecordMetric() {
        var builder = RestClient.builder().baseUrl("http://provider");
        var server = MockRestServiceServer.bindTo(builder).build();
        var metrics = new SimpleMeterRegistry();
        var client = new ResilientDeviceIntelligenceClient(builder.build(), metrics);
        server.expect(once(), requestTo("http://provider/internal/device-intelligence/risk-1?ipAddress=203.0.113.1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"deviceId":"risk-1","riskLevel":"HIGH","confidence":97,
                         "signals":["ROOTED_DEVICE"],"fallback":false}
                        """, MediaType.APPLICATION_JSON));

        var result = client.analyze("risk-1", "203.0.113.1");

        assertThat(result.riskLevel()).isEqualTo(DeviceRiskLevel.HIGH);
        assertThat(metrics.counter("fraud.device.intelligence", "outcome", "success", "risk", "HIGH").count())
                .isEqualTo(1);
        server.verify();
    }

    @Test
    void shouldReturnConservativeFallback() {
        var metrics = new SimpleMeterRegistry();
        var client = new ResilientDeviceIntelligenceClient(RestClient.create(), metrics);

        var result = client.fallback("device-1", "203.0.113.1", new RuntimeException("offline"));

        assertThat(result.riskLevel()).isEqualTo(DeviceRiskLevel.UNKNOWN);
        assertThat(result.fallback()).isTrue();
        assertThat(metrics.counter("fraud.device.intelligence", "outcome", "fallback").count()).isEqualTo(1);
    }
}
