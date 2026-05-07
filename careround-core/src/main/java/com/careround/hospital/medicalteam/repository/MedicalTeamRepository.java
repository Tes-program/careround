package com.careround.hospital.medicalteam.repository;

import com.careround.hospital.medicalteam.entity.MedicalTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalTeamRepository extends JpaRepository<MedicalTeam, String> {
}

