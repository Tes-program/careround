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
    @Bean public NewTopic patientUpdated() { return topic("patient-updated"); }
    @Bean public NewTopic prescriptionConfirmed() { return topic("prescription-confirmed"); }
    @Bean public NewTopic prescriptionDiscontinued() { return topic("prescription-discontinued"); }
    @Bean public NewTopic medicationChartCreated() { return topic("medication-chart-created"); }
    @Bean public NewTopic medicationTaskReminder() { return topic("medication-task-reminder"); }
    @Bean public NewTopic medicationTaskCompleted() { return topic("medication-task-completed"); }
    @Bean public NewTopic medicationTaskOverdue() { return topic("medication-task-overdue"); }
    @Bean public NewTopic clinicalNoteSaved() { return topic("clinical-note-saved"); }
    @Bean public NewTopic vitalsRecorded() { return topic("vitals-recorded"); }
    @Bean public NewTopic hospitalOnboardingRequested() { return topic("hospital-onboarding-requested"); }
    @Bean public NewTopic hospitalOnboardingReviewed() { return topic("hospital-onboarding-reviewed"); }
    @Bean public NewTopic hospitalProvisioned() { return topic("hospital-provisioned"); }
    @Bean public NewTopic userActivationRequested() { return topic("user-activation-requested"); }
    @Bean public NewTopic manualMedicationAdded() { return topic("manual-medication-added"); }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }
}
