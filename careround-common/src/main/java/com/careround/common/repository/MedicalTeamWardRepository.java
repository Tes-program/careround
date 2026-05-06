package com.careround.common.repository;

import com.careround.common.entity.MedicalTeamWard;
import com.careround.common.entity.MedicalTeamWardId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalTeamWardRepository extends JpaRepository<MedicalTeamWard, MedicalTeamWardId> {
    List<MedicalTeamWard> findByIdMedicalTeamId(String medicalTeamId);
    List<MedicalTeamWard> findByIdWardId(String wardId);
    boolean existsByIdMedicalTeamIdAndIdWardId(String medicalTeamId, String wardId);
    void deleteByIdMedicalTeamIdAndIdWardId(String medicalTeamId, String wardId);
}
