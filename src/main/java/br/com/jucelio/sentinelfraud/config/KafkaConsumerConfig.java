package br.com.jucelio.sentinelfraud.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
            @Value("${kafka.consumer.retry-attempts:3}") long attempts,
            @Value("${kafka.consumer.retry-backoff:500}") long backoff) {
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(backoff, Math.max(0, attempts - 1)));
    }

    @Bean
    NewTopic fraudDecisionTopic(@Value("${kafka.topics.fraud-decisions}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic fraudDecisionDltTopic(@Value("${kafka.topics.fraud-decisions-dlt}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic fraudDecisionReplayTopic(@Value("${kafka.topics.fraud-decisions-replay}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
