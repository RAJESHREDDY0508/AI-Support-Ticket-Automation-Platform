package com.ticketplatform.ticketservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topic.ticket-created:ticket.created.v1}")
    private String topicTicketCreated;

    @Value("${app.kafka.topic.ticket-ai-processed:ticket.ai_processed.v1}")
    private String topicTicketAiProcessed;

    @Value("${app.kafka.topic.ticket-response-approved:ticket.response_approved.v1}")
    private String topicTicketResponseApproved;

    @Value("${app.kafka.topic.dlq:ticket.dlq.v1}")
    private String topicDlq;

    @Bean
    public NewTopic topicTicketCreated() {
        return TopicBuilder.name(topicTicketCreated).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicTicketAiProcessed() {
        return TopicBuilder.name(topicTicketAiProcessed).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicTicketResponseApproved() {
        return TopicBuilder.name(topicTicketResponseApproved).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic topicDlq() {
        return TopicBuilder.name(topicDlq).partitions(1).replicas(1).build();
    }
}
