package com.careround.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;

    @Bean public NewTopic patientAdmitted() { return topic("careround.patient.admitted"); }
    @Bean public NewTopic patientDischarged() { return topic("careround.patient.discharged"); }
    @Bean public NewTopic patientDeterioration() { return topic("careround.patient.deterioration"); }
    @Bean public NewTopic vitalsRecorded() { return topic("careround.vitals.recorded"); }
    @Bean public NewTopic noteCreated() { return topic("careround.note.created"); }
    @Bean public NewTopic userInvited() { return topic("careround.user.invited"); }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }
}
