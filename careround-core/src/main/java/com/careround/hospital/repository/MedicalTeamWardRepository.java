package com.careround.hospital.repository;

import com.careround.hospital.entity.MedicalTeamWard;
import com.careround.hospital.entity.MedicalTeamWardId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalTeamWardRepository extends JpaRepository<MedicalTeamWard, MedicalTeamWardId> {

    List<MedicalTeamWard> findAllByIdMedicalTeamId(String medicalTeamId);
}
