package com.careround.common.repository;

import com.careround.common.entity.MedicalTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MedicalTeamRepository extends JpaRepository<MedicalTeam, String> {
    List<MedicalTeam> findByHospitalId(String hospitalId);
    Optional<MedicalTeam> findByIdAndHospitalId(String id, String hospitalId);
    List<MedicalTeam> findByConsultantId(String consultantId);
    List<MedicalTeam> findByDepartmentId(String departmentId);
}
