package com.careround.shared.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;

    @Bean public NewTopic patientAdmitted() { return topic("patient-admitted"); }
    @Bean public NewTopic patientDischarged() { return topic("patient-discharged"); }
    @Bean public NewTopic prescriptionConfirmed() { return topic("prescription-confirmed"); }
    @Bean public NewTopic medicationChartCreated() { return topic("medication-chart-created"); }
    @Bean public NewTopic medicationTaskOverdue() { return topic("medication-task-overdue"); }
    @Bean public NewTopic clinicalNoteSaved() { return topic("clinical-note-saved"); }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }
}
