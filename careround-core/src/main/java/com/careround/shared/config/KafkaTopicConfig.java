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
    @Bean public NewTopic shiftCreated() { return topic("careround.shift.created"); }
    @Bean public NewTopic shiftActivated() { return topic("careround.shift.activated"); }
    @Bean public NewTopic roundCompleted() { return topic("careround.round.completed"); }
    @Bean public NewTopic handoverCompleted() { return topic("careround.handover.completed"); }
    @Bean public NewTopic taskOverdue() { return topic("careround.task.overdue"); }
    @Bean public NewTopic patientDeterioration() { return topic("careround.patient.deterioration"); }
    @Bean public NewTopic escalationUnacknowledged() { return topic("careround.escalation.unacknowledged"); }
    @Bean public NewTopic patientDischargeReady() { return topic("careround.patient.discharge-ready"); }
    @Bean public NewTopic patientDischarged() { return topic("careround.patient.discharged"); }
    @Bean public NewTopic teamInviteSent() { return topic("careround.team.invite-sent"); }
    @Bean public NewTopic teamMemberAdded() { return topic("careround.team.member-added"); }
    @Bean public NewTopic inviteExpired() { return topic("careround.invite.expired"); }
    @Bean public NewTopic hospitalOnboardingRequested() { return topic("careround.hospital.onboarding_requested"); }
    @Bean public NewTopic hospitalOnboardingReviewed() { return topic("careround.hospital.onboarding_reviewed"); }
    @Bean public NewTopic hospitalProvisioned() { return topic("careround.hospital.provisioned"); }
    @Bean public NewTopic userActivationRequested() { return topic("careround.user.activation_requested"); }
    @Bean public NewTopic careTaskWorkloadConflict() { return topic("careround.care_task.workload_conflict"); }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }
}
