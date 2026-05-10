package com.careround.hospital.repository;

import com.careround.hospital.entity.MedicalTeamMember;
import com.careround.hospital.entity.MedicalTeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalTeamMemberRepository extends JpaRepository<MedicalTeamMember, MedicalTeamMemberId> {

    List<MedicalTeamMember> findAllByIdMedicalTeamId(String medicalTeamId);

    boolean existsByIdMedicalTeamIdAndIdUserId(String medicalTeamId, String userId);
}
