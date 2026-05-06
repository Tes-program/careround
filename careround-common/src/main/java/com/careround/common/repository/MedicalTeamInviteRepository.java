package com.careround.common.repository;

import com.careround.common.entity.MedicalTeamInvite;
import com.careround.common.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MedicalTeamInviteRepository extends JpaRepository<MedicalTeamInvite, String> {
    Optional<MedicalTeamInvite> findByIdAndInvitedUserId(String id, String invitedUserId);
    List<MedicalTeamInvite> findByInvitedUserIdAndStatus(String invitedUserId, InviteStatus status);
    List<MedicalTeamInvite> findByInvitedUserId(String invitedUserId);
    boolean existsByMedicalTeamIdAndInvitedUserIdAndStatus(String medicalTeamId, String invitedUserId, InviteStatus status);

    @Modifying
    @Query("UPDATE MedicalTeamInvite i SET i.status = 'EXPIRED' " +
           "WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    int expirePendingInvites(@Param("now") LocalDateTime now);
}
