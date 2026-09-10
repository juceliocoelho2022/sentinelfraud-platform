package br.com.jucelio.sentinelfraud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
    @Bean OpenAPI api() { return new OpenAPI().info(new Info().title("SentinelFraud API").version("v1").description("Real-time fraud decision engine")); }
}
