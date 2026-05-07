package com.careround.hospital.repository;

import com.careround.hospital.entity.MedicalTeamInvite;
import com.careround.hospital.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalTeamInviteRepository extends JpaRepository<MedicalTeamInvite, String> {

    List<MedicalTeamInvite> findAllByInvitedUserIdAndStatus(String userId, InviteStatus status);

    boolean existsByMedicalTeamIdAndInvitedUserIdAndStatus(String teamId, String userId, InviteStatus status);

    List<MedicalTeamInvite> findAllByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime now);
}
