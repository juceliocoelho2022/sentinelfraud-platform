package br.com.jucelio.sentinelfraud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class DeviceIntelligenceConfig {

    @Bean("deviceIntelligenceRestClient")
    RestClient deviceIntelligenceRestClient(
            RestClient.Builder builder,
            @Value("${fraud.device-intelligence.base-url}") String baseUrl,
            @Value("${fraud.device-intelligence.connect-timeout}") Duration connectTimeout,
            @Value("${fraud.device-intelligence.read-timeout}") Duration readTimeout) {
        var httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        return builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }
}
