package com.careround.hospital.medicalteam.repository;

import com.careround.hospital.medicalteam.entity.MedicalTeamInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalTeamInviteRepository extends JpaRepository<MedicalTeamInvite, String> {
}

