package com.careround.common.repository;

import com.careround.common.entity.MedicalTeamMember;
import com.careround.common.entity.MedicalTeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalTeamMemberRepository extends JpaRepository<MedicalTeamMember, MedicalTeamMemberId> {
    List<MedicalTeamMember> findByIdMedicalTeamId(String medicalTeamId);
    List<MedicalTeamMember> findByIdUserId(String userId);
    boolean existsByIdMedicalTeamIdAndIdUserId(String medicalTeamId, String userId);
    void deleteByIdMedicalTeamIdAndIdUserId(String medicalTeamId, String userId);
}
