package br.com.jucelio.sentinelfraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SentinelFraudApplication {
    public static void main(String[] args) { SpringApplication.run(SentinelFraudApplication.class, args); }
}
