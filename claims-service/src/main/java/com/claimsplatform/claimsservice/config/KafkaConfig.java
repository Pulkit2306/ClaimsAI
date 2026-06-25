package com.claimsplatform.claimsservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic claimCreatedTopic() {
        return TopicBuilder.name("claim.created").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic claimUpdatedTopic() {
        return TopicBuilder.name("claim.updated").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic claimStatusChangedTopic() {
        return TopicBuilder.name("claim.status.changed").partitions(3).replicas(1).build();
    }
}
