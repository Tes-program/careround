package com.careround.hospital.medicalteam.repository;

import com.careround.hospital.medicalteam.entity.MedicalTeamMember;
import com.careround.hospital.medicalteam.entity.MedicalTeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalTeamMemberRepository extends JpaRepository<MedicalTeamMember, MedicalTeamMemberId> {
}

