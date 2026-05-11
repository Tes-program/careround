package com.careround.scheduler.jobs;

import com.careround.hospital.entity.MedicalTeamInvite;
import com.careround.hospital.enums.InviteStatus;
import com.careround.hospital.repository.MedicalTeamInviteRepository;
import com.careround.scheduler.service.InviteExpiryProcessor;
import com.careround.shared.event.InviteExpiredEvent;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InviteExpiryJobTest {

    @Mock
    private MedicalTeamInviteRepository medicalTeamInviteRepository;

    @Mock
    private OutboxService outboxService;

    private InviteExpiryProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new InviteExpiryProcessor(medicalTeamInviteRepository, outboxService);
    }

    @Test
    void expiredPendingInvite_setsStatusAndPublishesEvent() {
        MedicalTeamInvite invite = invite(InviteStatus.PENDING, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        when(medicalTeamInviteRepository.findAllByStatusAndExpiresAtBefore(eq(InviteStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(invite));

        int processed = processor.expirePendingInvites("corr-1");

        assertThat(processed).isEqualTo(1);
        assertThat(invite.getStatus()).isEqualTo(InviteStatus.EXPIRED);
        verify(outboxService).publish(eq("INVITE_EXPIRED"), any(InviteExpiredEvent.class), eq("hosp-1"));
        verify(medicalTeamInviteRepository).save(invite);
    }

    @Test
    void nonExpiredInvite_skipped() {
        MedicalTeamInvite invite = invite(InviteStatus.PENDING, LocalDateTime.now(ZoneOffset.UTC).plusMinutes(10));
        when(medicalTeamInviteRepository.findAllByStatusAndExpiresAtBefore(eq(InviteStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(invite));

        int processed = processor.expirePendingInvites("corr-1");

        assertThat(processed).isZero();
        verify(outboxService, never()).publish(any(), any(), any());
    }

    @Test
    void alreadyAcceptedInvite_skipped() {
        MedicalTeamInvite invite = invite(InviteStatus.ACCEPTED, LocalDateTime.now(ZoneOffset.UTC).minusMinutes(10));
        when(medicalTeamInviteRepository.findAllByStatusAndExpiresAtBefore(eq(InviteStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(invite));

        int processed = processor.expirePendingInvites("corr-1");

        assertThat(processed).isZero();
        assertThat(invite.getStatus()).isEqualTo(InviteStatus.ACCEPTED);
        verify(outboxService, never()).publish(any(), any(), any());
    }

    private MedicalTeamInvite invite(InviteStatus status, LocalDateTime expiresAt) {
        MedicalTeamInvite invite = new MedicalTeamInvite();
        invite.setId("invite-1");
        invite.setHospitalId("hosp-1");
        invite.setMedicalTeamId("team-1");
        invite.setInvitedUserId("user-1");
        invite.setInvitedById("consultant-1");
        invite.setStatus(status);
        invite.setExpiresAt(expiresAt);
        return invite;
    }
}
