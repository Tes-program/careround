package com.careround.hospital.medicalteam;

import com.careround.auth.entity.User;
import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.MedicalTeamInvite;
import com.careround.hospital.entity.MedicalTeamMemberId;
import com.careround.hospital.entity.MedicalTeamWardId;
import com.careround.hospital.entity.Ward;
import com.careround.hospital.enums.InviteStatus;
import com.careround.hospital.medicalteam.dto.AssignWardRequest;
import com.careround.hospital.medicalteam.dto.CreateMedicalTeamRequest;
import com.careround.hospital.medicalteam.dto.InviteResponse;
import com.careround.hospital.medicalteam.dto.MedicalTeamResponse;
import com.careround.hospital.medicalteam.dto.SendInviteRequest;
import com.careround.hospital.repository.MedicalTeamInviteRepository;
import com.careround.hospital.repository.MedicalTeamMemberRepository;
import com.careround.hospital.repository.MedicalTeamRepository;
import com.careround.hospital.repository.MedicalTeamWardRepository;
import com.careround.hospital.repository.WardRepository;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicalTeamServiceTest {

    @Mock private MedicalTeamRepository medicalTeamRepository;
    @Mock private MedicalTeamMemberRepository memberRepository;
    @Mock private MedicalTeamWardRepository teamWardRepository;
    @Mock private MedicalTeamInviteRepository inviteRepository;
    @Mock private WardRepository wardRepository;
    @Mock private UserRepository userRepository;
    @Mock private OutboxService outboxService;

    @InjectMocks private MedicalTeamServiceImpl medicalTeamService;

    private static final String HOSPITAL_ID = "hosp-1";
    private static final String CONSULTANT_ID = "user-consultant";
    private static final String TEAM_ID = "team-1";

    private MedicalTeam team;
    private User targetUser;

    @BeforeEach
    void setUp() {
        team = new MedicalTeam();
        team.setId(TEAM_ID);
        team.setHospitalId(HOSPITAL_ID);
        team.setName("Alpha Team");
        team.setConsultantId(CONSULTANT_ID);
        team.setDepartmentId("dept-1");

        targetUser = new User();
        targetUser.setId("user-target");
        targetUser.setHospitalId(HOSPITAL_ID);
        targetUser.setRole(UserRole.REGISTRAR);
    }

    @Test
    void create_happyPath_shouldSaveTeamWithConsultantId() {
        when(medicalTeamRepository.save(any())).thenAnswer(i -> {
            MedicalTeam t = i.getArgument(0);
            t.setId(TEAM_ID);
            return t;
        });

        MedicalTeamResponse result = medicalTeamService.create(HOSPITAL_ID, CONSULTANT_ID,
                new CreateMedicalTeamRequest("Alpha Team", "dept-1", null));

        assertThat(result.consultantId()).isEqualTo(CONSULTANT_ID);
        assertThat(result.hospitalId()).isEqualTo(HOSPITAL_ID);
    }

    @Test
    void create_withExplicitConsultantId_shouldUseProvidedId() {
        String otherConsultant = "other-consultant";
        when(medicalTeamRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MedicalTeamResponse result = medicalTeamService.create(HOSPITAL_ID, CONSULTANT_ID,
                new CreateMedicalTeamRequest("Beta Team", "dept-1", otherConsultant));

        assertThat(result.consultantId()).isEqualTo(otherConsultant);
    }

    @Test
    void sendInvite_happyPath_shouldSaveInviteAndPublishEvent() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(userRepository.findByIdAndHospitalId("user-target", HOSPITAL_ID))
                .thenReturn(Optional.of(targetUser));
        when(memberRepository.existsByIdMedicalTeamIdAndIdUserId(TEAM_ID, "user-target"))
                .thenReturn(false);
        when(inviteRepository.existsByMedicalTeamIdAndInvitedUserIdAndStatus(TEAM_ID, "user-target", InviteStatus.PENDING))
                .thenReturn(false);
        when(inviteRepository.save(any())).thenAnswer(i -> {
            MedicalTeamInvite inv = i.getArgument(0);
            inv.setId("invite-1");
            return inv;
        });

        InviteResponse result = medicalTeamService.sendInvite(HOSPITAL_ID, TEAM_ID,
                CONSULTANT_ID, new SendInviteRequest("user-target"));

        assertThat(result.id()).isEqualTo("invite-1");
        assertThat(result.status()).isEqualTo(InviteStatus.PENDING);
        verify(outboxService).publish(eq("TEAM_INVITE_SENT"), any(), eq(HOSPITAL_ID));
    }

    @Test
    void sendInvite_onlyTeamConsultantCanSendInvite_throwsAccessDenied() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));

        assertThatThrownBy(() -> medicalTeamService.sendInvite(HOSPITAL_ID, TEAM_ID,
                "not-the-consultant", new SendInviteRequest("user-target")))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("consultant");
    }

    @Test
    void sendInvite_toUserInDifferentHospital_throwsBusinessRuleException() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(userRepository.findByIdAndHospitalId("user-other-hosp", HOSPITAL_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalTeamService.sendInvite(HOSPITAL_ID, TEAM_ID,
                CONSULTANT_ID, new SendInviteRequest("user-other-hosp")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not found in this hospital");
    }

    @Test
    void sendInvite_duplicatePendingInvite_throwsBusinessRuleException() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(userRepository.findByIdAndHospitalId("user-target", HOSPITAL_ID))
                .thenReturn(Optional.of(targetUser));
        when(memberRepository.existsByIdMedicalTeamIdAndIdUserId(TEAM_ID, "user-target"))
                .thenReturn(false);
        when(inviteRepository.existsByMedicalTeamIdAndInvitedUserIdAndStatus(TEAM_ID, "user-target", InviteStatus.PENDING))
                .thenReturn(true);

        assertThatThrownBy(() -> medicalTeamService.sendInvite(HOSPITAL_ID, TEAM_ID,
                CONSULTANT_ID, new SendInviteRequest("user-target")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pending invite already exists");
    }

    @Test
    void sendInvite_toAlreadyExistingMember_throwsBusinessRuleException() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(userRepository.findByIdAndHospitalId("user-target", HOSPITAL_ID))
                .thenReturn(Optional.of(targetUser));
        when(memberRepository.existsByIdMedicalTeamIdAndIdUserId(TEAM_ID, "user-target"))
                .thenReturn(true);

        assertThatThrownBy(() -> medicalTeamService.sendInvite(HOSPITAL_ID, TEAM_ID,
                CONSULTANT_ID, new SendInviteRequest("user-target")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already a member");
    }

    @Test
    void getById_withUnknownTeam_throwsResourceNotFoundException() {
        when(medicalTeamRepository.findByIdAndHospitalId("bad", HOSPITAL_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalTeamService.getById(HOSPITAL_ID, "bad"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeMember_byNonConsultant_throwsAccessDenied() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));

        assertThatThrownBy(() -> medicalTeamService.removeMember(HOSPITAL_ID, TEAM_ID,
                "user-target", "some-other-user"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void removeMember_happyPath_shouldDeleteMember() {
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        MedicalTeamMemberId memberId = new MedicalTeamMemberId(TEAM_ID, "user-target");
        when(memberRepository.existsById(memberId)).thenReturn(true);

        medicalTeamService.removeMember(HOSPITAL_ID, TEAM_ID, "user-target", CONSULTANT_ID);

        verify(memberRepository).deleteById(memberId);
    }

    @Test
    void assignWard_happyPath_shouldCreateTeamWardAssignment() {
        Ward ward = new Ward();
        ward.setId("ward-1");
        ward.setHospitalId(HOSPITAL_ID);
        when(medicalTeamRepository.findByIdAndHospitalId(TEAM_ID, HOSPITAL_ID))
                .thenReturn(Optional.of(team));
        when(wardRepository.findByIdAndHospitalId("ward-1", HOSPITAL_ID))
                .thenReturn(Optional.of(ward));
        when(teamWardRepository.existsById(new MedicalTeamWardId(TEAM_ID, "ward-1")))
                .thenReturn(false);

        MedicalTeamResponse result = medicalTeamService.assignWard(
                HOSPITAL_ID, TEAM_ID, CONSULTANT_ID, new AssignWardRequest("ward-1"));

        assertThat(result.id()).isEqualTo(TEAM_ID);
        verify(teamWardRepository).save(any());
    }

    @Test
    void listPendingInvites_shouldReturnPendingInvitesForUser() {
        MedicalTeamInvite invite = new MedicalTeamInvite();
        invite.setId("invite-1");
        invite.setHospitalId(HOSPITAL_ID);
        invite.setMedicalTeamId(TEAM_ID);
        invite.setInvitedUserId("user-target");
        invite.setStatus(InviteStatus.PENDING);
        invite.setExpiresAt(LocalDateTime.now().plusHours(24));

        when(inviteRepository.findAllByInvitedUserIdAndStatus("user-target", InviteStatus.PENDING))
                .thenReturn(List.of(invite));

        List<InviteResponse> results = medicalTeamService.listPendingInvites("user-target");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().status()).isEqualTo(InviteStatus.PENDING);
    }
}
