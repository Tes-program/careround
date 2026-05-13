package com.careround.hospital.medicalteam;

import com.careround.auth.enums.UserRole;
import com.careround.auth.repository.UserRepository;
import com.careround.hospital.entity.MedicalTeam;
import com.careround.hospital.entity.MedicalTeamInvite;
import com.careround.hospital.entity.MedicalTeamMember;
import com.careround.hospital.entity.MedicalTeamMemberId;
import com.careround.hospital.entity.MedicalTeamWard;
import com.careround.hospital.entity.MedicalTeamWardId;
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
import com.careround.shared.event.TeamInviteSentEvent;
import com.careround.shared.event.TeamMemberAddedEvent;
import com.careround.shared.exception.AccessDeniedException;
import com.careround.shared.exception.BusinessRuleException;
import com.careround.shared.exception.ResourceNotFoundException;
import com.careround.shared.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalTeamServiceImpl implements MedicalTeamService {

    private final MedicalTeamRepository medicalTeamRepository;
    private final MedicalTeamMemberRepository memberRepository;
    private final MedicalTeamWardRepository teamWardRepository;
    private final MedicalTeamInviteRepository inviteRepository;
    private final WardRepository wardRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public MedicalTeamResponse create(String hospitalId, String requestingUserId, CreateMedicalTeamRequest request) {
        String consultantId = request.consultantId() != null ? request.consultantId() : requestingUserId;
        MedicalTeam team = new MedicalTeam();
        team.setHospitalId(hospitalId);
        team.setName(request.name());
        team.setConsultantId(consultantId);
        team.setDepartmentId(request.departmentId());
        return toResponse(medicalTeamRepository.save(team));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalTeamResponse getById(String hospitalId, String teamId) {
        return medicalTeamRepository.findByIdAndHospitalId(teamId, hospitalId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalTeamResponse> listByHospital(String hospitalId) {
        return medicalTeamRepository.findAllByHospitalId(hospitalId)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public InviteResponse sendInvite(String hospitalId, String teamId, String requestingUserId, SendInviteRequest request) {
        MedicalTeam team = medicalTeamRepository.findByIdAndHospitalId(teamId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));

        if (!team.getConsultantId().equals(requestingUserId)) {
            throw new AccessDeniedException("Only the team consultant can send invites");
        }

        String invitedUserId = request.invitedUserId();
        userRepository.findByIdAndHospitalId(invitedUserId, hospitalId)
                .orElseThrow(() -> new BusinessRuleException("Invited user not found in this hospital"));

        if (memberRepository.existsByIdMedicalTeamIdAndIdUserId(teamId, invitedUserId)) {
            throw new BusinessRuleException("User is already a member of this team");
        }

        if (inviteRepository.existsByMedicalTeamIdAndInvitedUserIdAndStatus(teamId, invitedUserId, InviteStatus.PENDING)) {
            throw new BusinessRuleException("A pending invite already exists for this user");
        }

        MedicalTeamInvite invite = new MedicalTeamInvite();
        invite.setHospitalId(hospitalId);
        invite.setMedicalTeamId(teamId);
        invite.setInvitedUserId(invitedUserId);
        invite.setInvitedById(requestingUserId);
        invite.setStatus(InviteStatus.PENDING);
        invite.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(48));
        invite = inviteRepository.save(invite);

        outboxService.publish("TEAM_INVITE_SENT",
                new TeamInviteSentEvent(
                        hospitalId,
                        invite.getId(),
                        invite.getMedicalTeamId(),
                        invite.getInvitedUserId(),
                        invite.getInvitedById(),
                        invite.getExpiresAt(),
                        MDC.get("correlationId")
                ),
                hospitalId);
        return toInviteResponse(invite);
    }

    @Override
    @Transactional
    public void acceptInvite(String hospitalId, String inviteId, String userId) {
        MedicalTeamInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (!invite.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Access denied");
        }
        if (!invite.getInvitedUserId().equals(userId)) {
            throw new AccessDeniedException("You can only accept your own invites");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BusinessRuleException("Invite is no longer pending");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            invite.setStatus(InviteStatus.EXPIRED);
            throw new BusinessRuleException("Invite has expired");
        }

        invite.setStatus(InviteStatus.ACCEPTED);

        MedicalTeamMember member = new MedicalTeamMember();
        member.setId(new MedicalTeamMemberId(invite.getMedicalTeamId(), userId));
        member.setJoinedAt(LocalDateTime.now(ZoneOffset.UTC));
        memberRepository.save(member);

        outboxService.publish("TEAM_MEMBER_ADDED",
                new TeamMemberAddedEvent(
                        hospitalId,
                        invite.getId(),
                        invite.getMedicalTeamId(),
                        userId,
                        invite.getInvitedById(),
                        LocalDateTime.now(ZoneOffset.UTC),
                        MDC.get("correlationId")
                ),
                hospitalId);
    }

    @Override
    @Transactional
    public void declineInvite(String hospitalId, String inviteId, String userId) {
        MedicalTeamInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (!invite.getHospitalId().equals(hospitalId)) {
            throw new AccessDeniedException("Access denied");
        }
        if (!invite.getInvitedUserId().equals(userId)) {
            throw new AccessDeniedException("You can only decline your own invites");
        }
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BusinessRuleException("Invite is no longer pending");
        }

        invite.setStatus(InviteStatus.DECLINED);
    }

    @Override
    @Transactional
    public void removeMember(String hospitalId, String teamId, String memberUserId, String requestingUserId) {
        MedicalTeam team = medicalTeamRepository.findByIdAndHospitalId(teamId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));

        assertTeamOwnerOrAdmin(hospitalId, team, requestingUserId);

        MedicalTeamMemberId memberId = new MedicalTeamMemberId(teamId, memberUserId);
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found in team");
        }
        memberRepository.deleteById(memberId);
    }

    @Override
    @Transactional
    public MedicalTeamResponse assignWard(String hospitalId, String teamId, String requestingUserId, AssignWardRequest request) {
        MedicalTeam team = medicalTeamRepository.findByIdAndHospitalId(teamId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
        assertTeamOwnerOrAdmin(hospitalId, team, requestingUserId);

        wardRepository.findByIdAndHospitalId(request.wardId(), hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        MedicalTeamWardId id = new MedicalTeamWardId(teamId, request.wardId());
        if (!teamWardRepository.existsById(id)) {
            MedicalTeamWard assignment = new MedicalTeamWard();
            assignment.setId(id);
            assignment.setAssignedAt(LocalDateTime.now(ZoneOffset.UTC));
            teamWardRepository.save(assignment);
        }
        return toResponse(team);
    }

    @Override
    @Transactional
    public void removeWard(String hospitalId, String teamId, String wardId, String requestingUserId) {
        MedicalTeam team = medicalTeamRepository.findByIdAndHospitalId(teamId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical team not found"));
        assertTeamOwnerOrAdmin(hospitalId, team, requestingUserId);

        wardRepository.findByIdAndHospitalId(wardId, hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward not found"));

        MedicalTeamWardId id = new MedicalTeamWardId(teamId, wardId);
        if (!teamWardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Medical team is not assigned to this ward");
        }
        teamWardRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InviteResponse> listPendingInvites(String userId) {
        return inviteRepository.findAllByInvitedUserIdAndStatus(userId, InviteStatus.PENDING)
                .stream().map(this::toInviteResponse).toList();
    }

    private void assertTeamOwnerOrAdmin(String hospitalId, MedicalTeam team, String requestingUserId) {
        if (team.getConsultantId().equals(requestingUserId)) {
            return;
        }

        boolean isAdmin = userRepository.findByIdAndHospitalId(requestingUserId, hospitalId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
        if (!isAdmin) {
            throw new AccessDeniedException("Only an admin or the team consultant can manage this team");
        }
    }

    private MedicalTeamResponse toResponse(MedicalTeam t) {
        List<String> wardIds = teamWardRepository.findAllByIdMedicalTeamId(t.getId()).stream()
                .map(assignment -> assignment.getId().getWardId())
                .toList();
        return new MedicalTeamResponse(t.getId(), t.getHospitalId(), t.getName(),
                t.getConsultantId(), t.getDepartmentId(), t.getCreatedAt(), wardIds);
    }

    private InviteResponse toInviteResponse(MedicalTeamInvite i) {
        return new InviteResponse(i.getId(), i.getHospitalId(), i.getMedicalTeamId(),
                i.getInvitedUserId(), i.getInvitedById(), i.getStatus(),
                i.getExpiresAt(), i.getCreatedAt());
    }
}
