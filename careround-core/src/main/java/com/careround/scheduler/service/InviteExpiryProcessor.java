package com.careround.scheduler.service;

import com.careround.hospital.entity.MedicalTeamInvite;
import com.careround.hospital.enums.InviteStatus;
import com.careround.hospital.repository.MedicalTeamInviteRepository;
import com.careround.shared.event.InviteExpiredEvent;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteExpiryProcessor {

    private final MedicalTeamInviteRepository medicalTeamInviteRepository;
    private final OutboxService outboxService;

    @Transactional
    public int expirePendingInvites(String correlationId) {
        List<MedicalTeamInvite> invites = medicalTeamInviteRepository.findAllByStatusAndExpiresAtBefore(
                InviteStatus.PENDING,
                LocalDateTime.now(ZoneOffset.UTC)
        );

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int processed = 0;
        for (MedicalTeamInvite invite : invites) {
            if (invite.getStatus() != InviteStatus.PENDING
                    || invite.getExpiresAt() == null
                    || !invite.getExpiresAt().isBefore(now)) {
                continue;
            }
            expireInvite(invite, correlationId);
            processed++;
        }

        return processed;
    }

    private void expireInvite(MedicalTeamInvite invite, String correlationId) {
        invite.setStatus(InviteStatus.EXPIRED);
        outboxService.publish(
                "INVITE_EXPIRED",
                new InviteExpiredEvent(
                        invite.getHospitalId(),
                        invite.getId(),
                        invite.getMedicalTeamId(),
                        invite.getInvitedUserId(),
                        invite.getInvitedById(),
                        LocalDateTime.now(ZoneOffset.UTC),
                        correlationId
                ),
                invite.getHospitalId()
        );
        medicalTeamInviteRepository.save(invite);
    }
}
