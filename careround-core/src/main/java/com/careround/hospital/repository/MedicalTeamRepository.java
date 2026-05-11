package com.careround.hospital.repository;

import com.careround.hospital.entity.MedicalTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalTeamRepository extends JpaRepository<MedicalTeam, String> {

    List<MedicalTeam> findAllByHospitalId(String hospitalId);

    Optional<MedicalTeam> findByIdAndHospitalId(String id, String hospitalId);

    List<MedicalTeam> findAllByConsultantIdAndHospitalId(String consultantId, String hospitalId);

    long countByHospitalId(String hospitalId);
}
