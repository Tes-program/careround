package com.careround.notification.service;

import com.careround.auth.enums.UserRole;
import com.careround.hospital.entity.MedicalTeamInvite;
import com.careround.hospital.enums.InviteStatus;
import com.careround.hospital.repository.MedicalTeamInviteRepository;
import com.careround.notification.dto.NotificationResponse;
import com.careround.notification.dto.UnreadCountResponse;
import com.careround.notification.entity.NotificationReadReceipt;
import com.careround.notification.repository.NotificationReadReceiptRepository;
import com.careround.patient.entity.CareTask;
import com.careround.patient.entity.Escalation;
import com.careround.patient.enums.EscalationStatus;
import com.careround.patient.enums.TaskStatus;
import com.careround.patient.repository.CareTaskRepository;
import com.careround.patient.repository.EscalationRepository;
import com.careround.shared.security.HospitalContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EscalationRepository escalationRepository;
    private final CareTaskRepository careTaskRepository;
    private final MedicalTeamInviteRepository inviteRepository;
    private final NotificationReadReceiptRepository readReceiptRepository;
    private final PersistedNotificationLookup persistedNotificationLookup;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications() {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();
        Set<String> readIds = readIds(hospitalId, userId);

        return Stream.concat(
                        Stream.concat(persistedNotifications(hospitalId, userId), escalationNotifications(hospitalId, userId)),
                        Stream.concat(taskNotifications(hospitalId, userId), inviteNotifications(userId)))
                .map(notification -> withReadState(notification, readIds.contains(notification.id())))
                .sorted(Comparator.comparing(NotificationResponse::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount() {
        long count = listNotifications().stream()
                .filter(notification -> !notification.read())
                .count();
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public void markRead(String notificationId) {
        saveReadReceipt(HospitalContextHolder.getHospitalId(), HospitalContextHolder.getUserId(), notificationId);
    }

    @Override
    @Transactional
    public void markAllRead() {
        String hospitalId = HospitalContextHolder.getHospitalId();
        String userId = HospitalContextHolder.getUserId();
        listNotifications().forEach(notification -> saveReadReceipt(hospitalId, userId, notification.id()));
    }

    private Stream<NotificationResponse> escalationNotifications(String hospitalId, String userId) {
        UserRole role = HospitalContextHolder.getRole();
        List<Escalation> escalations = escalationRepository.findAllByHospitalIdAndStatusOrderByCreatedAtDesc(
                hospitalId, EscalationStatus.OPEN);

        return escalations.stream()
                .filter(escalation -> role == UserRole.ADMIN
                        || role == UserRole.WARD_SUPERVISOR
                        || userId.equals(escalation.getAssignedToId())
                        || userId.equals(escalation.getTriggeredById()))
                .map(escalation -> new NotificationResponse(
                        "escalation:" + escalation.getId(),
                        "ESCALATION",
                        escalation.getSeverity() + " escalation",
                        "Patient escalation requires review.",
                        "/patients/" + escalation.getPatientId() + "/escalations",
                        false,
                        escalation.getCreatedAt()));
    }

    private Stream<NotificationResponse> persistedNotifications(String hospitalId, String userId) {
        UserRole role = HospitalContextHolder.getRole();
        return persistedNotificationLookup.findForUser(hospitalId, userId, role).stream()
                .map(notification -> new NotificationResponse(
                        "delivery:" + notification.id(),
                        notification.eventType(),
                        notification.subject() != null ? notification.subject() : titleFromEventType(notification.eventType()),
                        notification.body(),
                        routeFromEventType(notification.eventType()),
                        false,
                        notification.createdAt()));
    }

    private Stream<NotificationResponse> taskNotifications(String hospitalId, String userId) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<CareTask> tasks = careTaskRepository.findAllByHospitalIdAndStatusInOrderByWindowEndAsc(
                hospitalId, List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.OVERDUE));

        return tasks.stream()
                .filter(task -> userId.equals(task.getAssignedToId()) || HospitalContextHolder.hasAnyRole(UserRole.ADMIN, UserRole.WARD_SUPERVISOR))
                .filter(task -> task.getStatus() == TaskStatus.OVERDUE || (task.getWindowEnd() != null && task.getWindowEnd().isBefore(now)))
                .map(task -> new NotificationResponse(
                        "care-task:" + task.getId(),
                        "CARE_TASK",
                        task.getStatus() == TaskStatus.OVERDUE ? "Overdue care task" : "Care task due",
                        task.getTitle(),
                        "/care-tasks/" + task.getId(),
                        false,
                        task.getWindowEnd() != null ? task.getWindowEnd() : task.getCreatedAt()));
    }

    private Stream<NotificationResponse> inviteNotifications(String userId) {
        return inviteRepository.findAllByInvitedUserIdAndStatus(userId, InviteStatus.PENDING).stream()
                .map(invite -> new NotificationResponse(
                        "medical-team-invite:" + invite.getId(),
                        "MEDICAL_TEAM_INVITE",
                        "Medical team invite",
                        "You have a pending medical team invitation.",
                        "/medical-teams/invites",
                        false,
                        invite.getCreatedAt()));
    }

    private Set<String> readIds(String hospitalId, String userId) {
        Set<String> ids = new HashSet<>();
        readReceiptRepository.findAllByHospitalIdAndUserId(hospitalId, userId)
                .forEach(receipt -> ids.add(receipt.getNotificationId()));
        return ids;
    }

    private NotificationResponse withReadState(NotificationResponse notification, boolean read) {
        return new NotificationResponse(notification.id(), notification.type(), notification.title(),
                notification.body(), notification.routeTarget(), read, notification.createdAt());
    }

    private void saveReadReceipt(String hospitalId, String userId, String notificationId) {
        readReceiptRepository.findByHospitalIdAndUserIdAndNotificationId(hospitalId, userId, notificationId)
                .orElseGet(() -> {
                    NotificationReadReceipt receipt = new NotificationReadReceipt();
                    receipt.setHospitalId(hospitalId);
                    receipt.setUserId(userId);
                    receipt.setNotificationId(notificationId);
                    receipt.setReadAt(LocalDateTime.now(ZoneOffset.UTC));
                    return readReceiptRepository.save(receipt);
                });
    }

    private String titleFromEventType(String eventType) {
        if (eventType == null) {
            return "Notification";
        }
        return switch (eventType) {
            case "careround.shift.created" -> "New shift created";
            case "careround.shift.activated" -> "Shift activated";
            case "careround.round.completed" -> "Round completed";
            case "careround.task.overdue" -> "Overdue task";
            case "careround.patient.deterioration" -> "Patient deterioration";
            case "careround.patient.discharged" -> "Patient discharged";
            case "careround.user.activation_requested" -> "Account activation";
            case "careround.care_task.workload_conflict" -> "Care task workload conflict";
            default -> eventType.replace("careround.", "").replace('.', ' ');
        };
    }

    private String routeFromEventType(String eventType) {
        if (eventType == null) {
            return "/dashboard";
        }
        return switch (eventType) {
            case "careround.shift.created", "careround.shift.activated" -> "/shifts";
            case "careround.round.completed" -> "/rounds";
            case "careround.task.overdue", "careround.care_task.workload_conflict" -> "/care-tasks";
            case "careround.patient.deterioration", "careround.patient.discharged" -> "/patients";
            case "careround.user.activation_requested" -> "/activate";
            default -> "/dashboard";
        };
    }
}
