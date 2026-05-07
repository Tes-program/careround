package com.careround.hospital.medicalteam.repository;

import com.careround.hospital.medicalteam.entity.MedicalTeamWard;
import com.careround.hospital.medicalteam.entity.MedicalTeamWardId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalTeamWardRepository extends JpaRepository<MedicalTeamWard, MedicalTeamWardId> {
}

